# apphelper2

# 使用手册

    appheler2: 最低兼容 29

## 1：修改statusBar颜色

    class App : Application() {
        override fun onCreate() {
            super.onCreate()
                val builder: Builder = Builder().apply {
                    statusBarColor = R.color.purple_700
                }
                BaseApplication.init(this, false, builder)
        }
    }    
 
## 2：修改titBar样式        
    如果要替换TitleBar的样式，可以把apphelper2中的R.layout.base_title_item 文件粘贴到自己的项目中，然后做对应的样式修改,但是不能修改文件名字和对应的id，否则就会导致异常。