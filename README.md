# a simple mvp frame for android 
## 介绍：一个使用mvp思想的简单开发框架
 ### BaseMvpActivity Activity Mvp实现的基类
 ### Contract 协约类 含有BaseView,BasePresenter
 ### Repositiory 数据操作类
## 使用方法：
 ### 1，首先定义接口实现继承Contract,内部定义View继承BaseView实现View相关的回调，内部定义Presenter继承BasePresenter接口声明Presenter方法，
 <img src="/preview/contract.jpeg">

 ### 2,定义类继承Repository实现方法，在getData中实现逻辑返回数据结果，在需要调用数据的地方dispatchUpdate方法
 <img src="/preview/repository.jpeg">


 ### 3,定义类继承XDevPresenter，泛型中传入对应的View和实现对应的Presenter接口在构造函数中初始化Repository，并且调用addRepository方法将Repository加入,实现方法参数传入Repository，注解Subcribe。方法体内接受结果。
 <img src="/preview/repository.jpeg">

 ### 4，activity继承MvpActivity,泛型传入对应的Contract的View，Presenter.实现对应的接口，initPresenter方法初始化Presenter类.如果界面已经继承其他类，只需在activity的onCreate方法实例化对应的Presenter并调用presenter的attachView方法(其他组件的生命周期开始时候调用)，onDestory方法中调用presenter的detachView方法（其他组件的生命周期结束时候调用）
>>>>>>> 6689c74e4fc0131eb2103a1e4a6a4aef210b060f
 <img src="/preview/activity.jpeg"> 



