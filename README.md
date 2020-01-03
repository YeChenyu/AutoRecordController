# AutoRecordController
远程控制录音、录屏、上送位置信息等

# 查看手机分辨率：
adb shell dumpsys window displays
adb shell wm size

# adb 屏幕录制
adb shell screenrecord --size 1280x720 --bit-rate 6000000 --time-limit 30 /sdcard/demo.mp4
 --size 指定视频分辨率；
 --bit-rate 指定视频比特率，默认为4M，该值越小，保存的视频文件越小；
 --time-limit 指定录制时长，若设定大于180，命令不会被执行

 ～备注：nable to get output buffers (err=-38) 模拟器暂不支持屏幕录制