package cn.vectorpeak.AIchat_projects

import android.app.Application
import cn.vectorpeak.AIchat_projects.data.AppContainer

class AIchatApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
