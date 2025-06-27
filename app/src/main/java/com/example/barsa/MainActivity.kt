package com.example.barsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.barsa.Navegator.MainNavigator
import com.example.barsa.Network.NetworkMonitor
import com.example.barsa.data.retrofit.ui.InventoryViewModel
import com.example.barsa.data.retrofit.ui.PapeletaViewModel
import com.example.barsa.data.retrofit.ui.UserViewModel
import com.example.barsa.data.room.TiemposViewModel
import com.example.barsa.ui.theme.BarsaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var networkMonitor: NetworkMonitor
    private val tiemposViewModel: TiemposViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val papeletaViewModel: PapeletaViewModel by viewModels()
    private val inventarioViewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkMonitor = NetworkMonitor(this)
        networkMonitor.register()
        setContent {
            BarsaTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainNavigator(tiemposViewModel, userViewModel, papeletaViewModel,inventarioViewModel )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BarsaTheme {
        Greeting("Android")
    }
}