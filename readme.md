# KeyboardHeightUtil
Activity在adjustNothing模式下对键盘高度的监听


- 可库可在activity的 “windowSoftInputMode” 属性设置为 “adjustNothing” 时检测软键盘的变化

- 正常情况下使用 “adjustNothing” 属性后视图不会发生任何变化，故也无法获知键盘高度和状态。此库是通过在activity中添加一个宽度为0，高度match_partent的PopupWindow，然后在PopupWindow中通过对ViewTreeObserver的监听来获取视图变化的

- 参考：

  [https://github.com/demongel/SoftKeyboardHeight]()
  - 在此库基础上修改完善，解决在刘海屏下测量不准确问题


