# Health Data Sample

This sample demonstrates receiving passive data updates in the background using the
`PassiveMonitoringClient` API.

### Running the sample

You will need a Wear device or emulator with Health Services installed. Open the sample project in
Android Studio and launch the app on your device or emulator.

On startup, the app checks whether heart rate data is available. If it is, you will see a screen
which will show heart rate count:


Use the switch to enable or disable passive data updates. The most recent measurement received is
shown below that.

Note that on some devices, it may take several minutes for a value to be returned and displayed.

On devices where heart rate data is not available, you will see a screen with empty state.

### how data fetch and stored

This will fetch data every 3 second and store it cache which is DataSore we are using.
Then after every 60 second we are saving data in Local Database which is RoomDatabase.
If data doesn't insert in Database then we keep data in our DataStore and we retry after every 60 second until it doesn't saved in database.

### Try it with synthetic data

With the sample running, you can turn on the sythetic data tracker by running the below command from
a shell. This will mimic the user performing an activity and generating heart rate data. Check the
app UI or logcat messages to see these data updates.

```shell
adb shell am broadcast \
-a "whs.USE_SYNTHETIC_PROVIDERS" \
com.google.android.wearable.healthservices
```

To see different heart rate values, try simulating different exercises:
```shell
# walking
adb shell am broadcast \
-a "whs.synthetic.user.START_WALKING" \
com.google.android.wearable.healthservices

# running
adb shell am broadcast \
-a "whs.synthetic.user.START_RUNNING" \
com.google.android.wearable.healthservices
```

To stop using the synthetic tracker, run this command:
```shell
adb shell am broadcast -a \
"whs.USE_SENSOR_PROVIDERS" \
com.google.android.wearable.healthservices
```
