package id.aseta.app

import AsetaNavGraph
import AuthViewModel
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.AssetViewerViewModel
import id.aseta.app.viewmodel.UHFViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel = AuthViewModel()
    private val uhfViewModel = UHFViewModel()
    private val assetViewerModel = AssetViewerViewModel()
    private val assetCategoryModel = AssetCategoryViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.checkSavedToken(this)
        setContent {
            if (viewModel.isCheckingLogin) {
                // sementara tunggu pengecekan token
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                AsetaNavGraph(viewModel,uhfViewModel,assetViewerModel,assetCategoryModel)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 139 || keyCode == 280 || keyCode == 293) {
            if (event?.repeatCount == 0) {
                lifecycleScope.launch {
                    KeyEventBus.emitKeyEvent(keyCode)

                    // Still update the scanning state in MainActivity if needed
                    UHFState.isScanning.value = !UHFState.isScanning.value
                    if (currentFocus != null) {
                        currentFocus!!.dispatchKeyEvent(event)
                    }
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

object KeyEventBus {
    private val _keyEvents = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val keyEvents = _keyEvents.asSharedFlow()

    // Function to emit key events
    suspend fun emitKeyEvent(keyCode: Int) {
        _keyEvents.emit(keyCode)
    }
}
