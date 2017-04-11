# a simple mvp frame for android 
## 介绍：一个使用mvp思想的简单开发框架,使用了部分Eventbus,agera代码何思想
 ### BaseMvpActivity Activity Mvp实现的基类
 ### Contract 协约类 含有BaseView,BasePresenter
 ### Repositiory 数据操作类
## 使用方法：
 ### 1，首先定义接口实现继承Contract,内部定义View继承BaseView实现View相关的回调，内部
 定义Presenter继承BasePresenter接口声明Presenter方法，
 /preview/contract.jpeg
 ### 2,定义类继承Repository实现方法，在getData中实现逻辑返回数据结果，在需要调用数据
 的地方dispatchUpdate方法
 /preview/repository.jpeg
 ### 3,定义类继承XDevPresenter，泛型中传入对应的View和实现对应的Presenter接口
 在构造函数中初始化Repository，并且调用addRepository方法将Repository加入
 实现方法参数传入Repository，注解Subcribe。方法体内接受结果。
 ### 4，activity继承MvpActivity,泛型传入对应的Contract的View，Presenter.实现对应的
 接口，initPresenter方法初始化Presenter类
 /preview/activity.jpeg
 



