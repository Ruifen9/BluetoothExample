##  OTA Source Code (JAVA)
> "app/src/main/java/com/bluexmicro/bluetoothexample/ota"

This directory is the OTA source code. Please copy to your project.

## How to use

OTA Example:
"app/src/main/java/com/bluexmicro/bluetoothexample/connection/ConnectionActivity.java"

```java
        OtaManager mgr = new OtaManager(ConnectionActivity.this, target);
        mgr.startFastOta(tasks.getValue(), new OtaManager.ProcessCallback() {

            @Override
            public void onProgress(int taskIndex, float progress) {
                // Because multiple tasks are being transmitted at once, the progress of each task will be printed sequentially
            }

            @Override
            public void onDone() {
            }

            @Override
            public void onFailed(int status) {
            }
        });
```
