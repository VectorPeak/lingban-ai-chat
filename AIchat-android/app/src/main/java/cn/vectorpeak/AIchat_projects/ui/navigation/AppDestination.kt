package cn.vectorpeak.AIchat_projects.ui.navigation

object AppDestination {
    const val Entry = "entry"
    const val Onboarding = "onboarding"
    const val Login = "login?roleKey={roleKey}"
    const val Home = "home"
    const val Chat = "chat/{roleKey}"

    fun login(roleKey: String?): String = "login?roleKey=${roleKey ?: "chenge"}"

    fun chat(roleKey: String): String = "chat/$roleKey"
}
