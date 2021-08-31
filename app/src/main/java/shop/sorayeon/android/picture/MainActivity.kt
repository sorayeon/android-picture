package shop.sorayeon.android.picture

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // 이미지 추가하기 버튼
    private val addPhotoButton: Button by lazy {
        findViewById<Button>(R.id.addPhotoButton)
    }

    // 전자액자 실행하기 버튼
    private val startPhotoFrameModeButton: Button by lazy {
        findViewById<Button>(R.id.startPhotoFrameModeButton)
    }

    // 썸네일을 표현할 이미지 View 목록
    private val imageViewList: List<ImageView> by lazy {
        mutableListOf<ImageView>().apply {
            add(findViewById<ImageView>(R.id.imageView11))
            add(findViewById<ImageView>(R.id.imageView12))
            add(findViewById<ImageView>(R.id.imageView13))
            add(findViewById<ImageView>(R.id.imageView21))
            add(findViewById<ImageView>(R.id.imageView22))
            add(findViewById<ImageView>(R.id.imageView23))
        }
    }

    // 이미지 목록
    private val imageUriList: MutableList<Uri> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 사진 추가하기 버튼 초기화
        initAddPhotoButton()
        // 전자액자 실행하기 버튼 초기화
        initStartPhotoFrameModeButton()
    }

    // 사진 추가하기 버튼 초기화
    private fun initAddPhotoButton() {
        // 사진 추가하기 버튼 클릭 이벤트
        addPhotoButton.setOnClickListener {
            when {
                // 앱에 저장소 읽기 권한이 이미 부여되어 있는지 확인
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 권한이 있다면 갤러리에서 사진을 선택
                    navigatePhotos()
                }
                // 이전에 권한 거부한 사용자의 경우 앱에 권한이 필요한 이유 설명 (교육용 팝업)
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }
                // 앱에 저장소 읽기 권한 요청
                else -> {
                    // 요청하려는 권한 배열과, 실행 후 전달받을 코드
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
                }
            }
        }
    }

    // 전자액자 실행하기 버튼 초기화
    private fun initStartPhotoFrameModeButton() {
        // 전자액자 실행하기 버튼 클릭 이벤트
        startPhotoFrameModeButton.setOnClickListener {
            // 전자액자 액티비티를 intent 에 담고
            val intent = Intent(this, PhotoFrameActivity::class.java)
            // 사진에 대한 Uri 정보를 photo$index 의 이름으로 넘겨준다.
            imageUriList.forEachIndexed { index, uri ->
                intent.putExtra("photo$index", uri.toString())
            }
            // 사진의 총 갯수를 같이 넘겨준다.
            intent.putExtra("photoListSize", imageUriList.size)
            // 전자액자 시작
            startActivity(intent)
        }
    }

    // 저장소 앨범 사진 선택
    private fun navigatePhotos() {
        var intent = Intent(Intent.ACTION_GET_CONTENT)
        // 타입 : 이미지
        intent.type = "image/*"
        // 새 엑티비티를 열어줌 + 결과값 전달 -> onActivityResult()
        startActivityForResult(intent, 2000)
    }

    // 교육용 팝업 알럿 다이얼로그
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("전자액자 앱에서 사진을 불러오기 위해 권한이 필요합니다.")
            // 동의한다면 앱에 저장소 읽기 권한 요청
            .setPositiveButton("동의하기") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            }
            // 거부한다면 그냥 알럿 닫기
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }

    // 앱의 저장소 읽기 권한 요청에 대한 응답처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 실행 후 콜백으로 전달받은 코드
        when (requestCode) {
            // 1000 이라면
            1000 -> {
                // 첫번째 배열의 권한 결과(저장소 읽기만 권한설정했으므로) 권한이 있다면
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 있다면 갤러리에서 사진을 선택
                    navigatePhotos()
                } else {
                    Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> { }
        }
    }

    // 사진 선택 후 선택한 사진을 넘겨 받을수 있는 콜백
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // RESULT_OK 인 경우 정상적으로 사진을 선택
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        // startActivityForResult() 호출시 넘긴 requestCode
        when (requestCode) {
            2000 -> {
                // 사진의 Uri 를 가져옴.
                val selectedImageUri: Uri? = data?.data
                // 선택한 사진이 있다면
                if (selectedImageUri != null) {
                    // 유효성검사 : 앨범 사진의 갯수는 6개까지만 가능
                    if (imageUriList.size == 6) {
                        Toast.makeText(this, "이미 사진이 꽉 찼습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    // List 에 선택한 사진을 담고
                    imageUriList.add(selectedImageUri)
                    // 이미지 뷰에 사진을 보여준다.
                    imageViewList[imageUriList.size - 1].setImageURI(selectedImageUri)
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}