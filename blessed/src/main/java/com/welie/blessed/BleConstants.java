package com.welie.blessed;

public class BleConstants {

    public static final String UUID_CCCD_CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb"; // for notify etc..

    public static final String UUID_GMS_SERVICE_GIMKIT_MAIN_SERVICE = "00000001-21A4-11E8-8812-000C2920EFFF"; // NEXGIM BLE service

    public static final String UUID_GMS_CHARACTERISTIC_BASE_COMMUNICATION = "00000002-21A4-11E8-8812-000C2920EFFF"; // handshake

    public static final String UUID_GMS_CHARACTERISTIC_FILE_TRANSFER = "00000003-21A4-11E8-8812-000C2920EFFF"; // file transfer

    public static final String UUID_GMS_CHARACTERISTIC_DEVICE_COMMAND_NOTIFY = "00000004-21A4-11E8-8812-000C2920EFFF"; // motor control/data notify etc..

    public static final String UUID_DFU_SERVICE_DFU = "8EC90003-F315-4F60-9FB8-838830DAEA50"; // DFU service

    public static final String UUID_DIS_SERVICE_DEVICE_INFORMATION = "8EC90003-F315-4F60-9FB8-838830DAEA50"; // DIS(Device Information Service)

    public static final String UUID_DIS_CHARACTERISTIC_DEVICE_ID = "00002A23-0000-1000-8000-00805F9B34FB"; // DIS characteristic : Device ID

    public static final String UUID_DIS_CHARACTERISTIC_DEVICE_MODEL = "00002A24-0000-1000-8000-00805F9B34FB"; // DIS characteristic : Device model

    public static final String UUID_HRS_SERVICE_HEART_RATE = "0000180D-0000-1000-8000-00805F9B34FB"; // HR service

    public static final String UUID_HRS_CHARACTERISTIC_HEART_RATE_MEASUREMENT = "00002A37-0000-1000-8000-00805F9B34FB"; // HR measurement characteristic for HR data

}
