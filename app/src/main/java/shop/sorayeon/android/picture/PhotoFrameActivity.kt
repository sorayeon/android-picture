package shop.sorayeon.android.picture

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.concurrent.timer

// 전자 액자 액티비티
class PhotoFrameActivity: AppCompatActivity() {

    // 앨범을 구성할 사진 목록
    private val photoList = mutableListOf<Uri>()

    // 현재 포지션 정보
    private var currentPosition = 0

    // 타이머 (
    private var timer: Timer? = null

    // 전자앨범 포토 이미지
    private val photoImageView: ImageView by lazy {
        findViewById<ImageView>(R.id.photoImageView)
    }

    // 전자앨범 포토 배경 이미지
    private val backgroundPhotoImageView: ImageView by lazy {
        findViewById<ImageView>(R.id.backgroundPhotoImageView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photoframe)

        Log.d("PhotoFrameActivity", "onCreate!!")
        // 메인 엑티비티에서 넘어온 사진을 photoList 에 담는다
        getPhotoUriFromIntent()
    }

    // 메인 엑티비티에서 넘어온 사진을 photoList 에 담는다
    private fun getPhotoUriFromIntent() {
        // 넘어온 사진의 사이즈를 담는다. (사진 URI 만 있다면 사이즈를 얻기 어려움)
        val size = intent.getIntExtra("photoListSize", 0)
        for (i in 0..size) {
            // 인덴트에서 사진의 URI 문자열을 가져와서
            intent.getStringExtra("photo$i")?.let {
                // URI 값이 있다면 파싱하여 앨벙의 사진목록 리스트에 담는다.
                photoList.add(Uri.parse(it))
            }
        }
    }

    // 앨범 동작 타이머 시작
    private fun startTimer() {
        // 5초 마다 실행하는 타이머 생성
        timer = timer(period = 5 * 1000) {
            runOnUiThread {
                Log.d("PhotoFrameActivity", "5초 지나감 !!")
                // 현재 사진 포지션 초기값 (0)
                val current = currentPosition
                // 다음 사진 포지션 -> 현재사진 포지션 + 1
                //    다음 포지션이 사진의 사이즈보다 크다면 -> 처음사진 초기값 (0)
                val next = if (photoList.size <= currentPosition + 1) 0 else currentPosition + 1

                // 배경이 되는 앨범사진을 현재 사진으로 넣고
                backgroundPhotoImageView.setImageURI(photoList[current])

                // 다음에 보여줄 사진의 알파값을 0으로 하고
                photoImageView.alpha = 0f
                // 다음에 보여줄 사진을 앨범에 넣는다.
                photoImageView.setImageURI(photoList[next])
                // 애니매이션을 통해 1초동안 알파값을 100% 로 바꿔준다.
                photoImageView.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .start()

                // 현재 포지션을 다음포지션으로 넣고 다음 타이머 동작(5초) 기다림
                currentPosition = next
            }
        }
    }

    override fun onStop() {
        super.onStop()

        Log.d("PhotoFrameActivity", "onStop!!! timer cancel")
        timer?.cancel()
    }

    override fun onStart() {
        super.onStart()
        Log.d("PhotoFrameActivity", "onStart!!! timer start")
        startTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PhotoFrameActivity", "onDestroy!!! timer cancel")
        timer?.cancel()
    }

}