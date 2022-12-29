# 潘多拉-图片视频选择器(功能完善阶段,勿用)

图片视频选择器,该项目是从https://github.com/getActivity/AndroidProject-Kotlin 中抽离出的一个图片视频选择器,之前没有
单独设置项目,我在使用过程中觉得这个挺方便好用的,所以为了方便使用,单独剥离出来,可以单独在项目中使用,后续我也会基于这个项目进行后续功能的迭代开发

[![](https://jitpack.io/v/com.gitee.clbDream/pdl-box_selector.svg)](https://jitpack.io/#com.gitee.clbDream/pdl-box_selector)

![](images/banner.png)

## 简介

这是一个Android开发工具库系列开源项目,不包含任何隐私信息的搜集,完全可以通过国内应用市场隐私合规的检测,现在开源,供广大Android开发程序员同胞使用,欢迎大家一起来进行维护

我也是个萌新程序员,项目有什么地方不足,请多多指教,哈哈

#### 作者的其他开源项目

* 多功能记录项目<事迹>
  ：[RecordThings-Android](https://github.com/clbDream/RecordThings-Android) ![](https://img.shields.io/github/stars/clbDream/RecordThings-Android.svg) ![](https://img.shields.io/github/forks/clbDream/RecordThings-Android.svg)
* 常用第三方库集合<库多多>
  ：[Pdlbox_Library](https://github.com/clbDream/Pdlbox_Library) ![](https://img.shields.io/github/stars/clbDream/Pdlbox_Library.svg) ![](https://img.shields.io/github/forks/clbDream/Pdlbox_Library.svg)
* 常用工具库集合<Tools>
  ：[Pdlbox_Tools](https://github.com/clbDream/Pdlbox_Tools) ![](https://img.shields.io/github/stars/clbDream/Pdlbox_Tools.svg) ![](https://img.shields.io/github/forks/clbDream/Pdlbox_Tools.svg)
* 尺寸库<Dimens>
  ：[Pdlbox_Dimens](https://github.com/clbDream/Pdlbox_Dimens) ![](https://img.shields.io/github/stars/clbDream/Pdlbox_Dimens.svg) ![](https://img.shields.io/github/forks/clbDream/Pdlbox_Dimens.svg)
* 轻量视频播放器<VideoPlayer>
  ：[Pdlbox_videoplayer](https://github.com/clbDream/Pdlbox_videoplayer) ![](https://img.shields.io/github/stars/clbDream/Pdlbox_videoplayer.svg) ![](https://img.shields.io/github/forks/clbDream/Pdlbox_videoplayer.svg)
* 图片视频选择器<VideoPlayer>
  ：[Pdlbox_videoplayer](https://github.com/clbDream/Pdlbox_selector) ![](https://img.shields.io/github/stars/clbDream/Pdlbox_selector.svg) ![](https://img.shields.io/github/forks/clbDream/Pdlbox_selector.svg)



## 如何使用(可参考Demo的使用)

1. Add the JitPack repository to your build file

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

2. Add the dependency(版本信息查看上面的标签里面)

```
dependencies {
	        implementation 'com.gitee.clbDream:pdl-box_selector:$version'
	}
```