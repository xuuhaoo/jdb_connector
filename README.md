# JDB Connector (After IDA attached and resume app ASAP without damned DDMS)

## Step1. What is this?
* This is an Gadget for `Reverse Engineer` to easily resume Android app through terminal command line without [DDMS(Dalvik Debug Monitor Server)](https://developer.android.com/studio/profile/monitor) AKA `Android Device Monitor` at all. Because Android Device Monitor was deprecated in Android Studio 3.1 and removed from Android Studio 3.2. 

## Step2. Assemble
* 1. Download `Jar` file to your computer.
* 2. Put your `JAVA_HOME` Env ready, make sure that in your `PATH`
* 3. `ANDROID_SDK_TOOL` Env need too, make sure that also in the `PATH` because we need to access `adb` eg. “/Library/Android/sdk/tools”
* 4. If your are using bash in your computer, than edit `~/.bash_profile` for add an alias for this java command. such as `alias jdbConnect=“java -jar xxx/xx/jdb_connect.jar”` source the file, don’t forget.

## Step3. Download

To [Release Page](https://github.com/xuuhaoo/jdb_connector/releases).

## Step4. How to use
* When you IDA Pro alerady attached to your App, than you wanna go on the next to dismiss `Waiting For Debugger` dialog, you should call as following command in your shell.

```shell
java -jar jdb_connect.jar -lp 8700 app_package_name
```
* If you want to use default `local port` setting (default port is 8700), you can call like this


```shell
java -jar jdb_connect.jar app_package_name
```
* If you following the `Step2` and set an alias, you can just call like this

```shell
jdbConnect app_package_name
```