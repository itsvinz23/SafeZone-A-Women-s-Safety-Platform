<!-- HEADER -->
<p align="center">
  <img src="https://d1lmq142maiv1z.cloudfront.net/NIBM_City_University_Logo_2_213b4dd2f6.svg" alt="NIBM City University Logo" width="200"/>
</p>

<h1 align="center">🛡️ SafeZone – A Women’s Safety Platform 👩‍🦰📱</h1>
<h3 align="center">Final Year Project – Higher National Diploma in Software Engineering</h3>

<p align="center">
  A mobile + IoT-based platform designed to enhance women’s safety through real-time tracking, SOS alerts, and wearable emergency devices.
</p>

<p align="center">
  <img src="https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExbm1ybHRpYWFxeG1oMTJqYjZyZTV4NjVmZXMybnh1bnA1OW8yaGRvMiZlcD12MV9naWZzX3NlYXJjaCZjdD1n/ZqlvCTNHpqrio/giphy.gif" width="300"/>
</p>

<p align="center">
  <a href="#"><img alt="Platform" src="https://img.shields.io/badge/platform-Android%20%7C%20ESP32-blueviolet?style=for-the-badge"></a>
  <a href="#"><img alt="Database" src="https://img.shields.io/badge/database-Firebase-orange?style=for-the-badge"></a>
  <a href="#"><img alt="License" src="https://img.shields.io/badge/license-MIT-green?style=for-the-badge"></a>
</p>

---

## 🚀 Overview

**SafeZone** is an IoT-powered **women’s safety solution** integrating a **mobile app** and a **wearable device** to provide quick SOS alerts, GPS tracking, and real-time safety monitoring.

This system ensures instant help during emergencies through **continuous alerts**, **real-time location sharing**, and **emergency proof collection**. 🆘📍

---

## 🎯 Objectives

- 🛰️ Enable **real-time location tracking**  
- ⚡ Send **immediate and repeated SOS alerts** to multiple contacts  
- 🎛️ Integrate an **IoT wearable button** for emergency activation  
- 📷 Capture **proof (photo/video)** during an incident  
- 🏠 Suggest **nearby safe places** for quick assistance  

---

## 🧠 System Overview

SafeZone includes **three main components**:

| Component | Description |
|------------|-------------|
| 📱 **Mobile App** | User login, SOS activation, location sharing, alerts, feedback |
| ⌚ **IoT Device** | Physical SOS button, GPS module, Wi-Fi communication |
| ☁️ **Firebase Cloud** | Stores real-time data, notifications, and location tracking |

---

## 🧩 Key Features

| 🧠 Feature | 💬 Description |
|------------|----------------|
| 🆘 **Quick SOS Activation** | Triggered via app or wearable button |
| 📡 **Continuous Tracking** | Real-time GPS monitoring and updates |
| 🔁 **Repeated Alerts** | Keeps notifying contacts until acknowledged |
| 🏃 **Safe Place Finder** | Uses Google Maps API to show nearest safety zones |
| 🗣️ **Voice Input & Proof Collection** | Records emergency audio/video |
| 💬 **Feedback System** | Collects user suggestions for improvement |

---

## 🧰 Technology Stack

| Component | Technologies |
|------------|--------------|
| **Mobile App** | Java (Android Studio) |
| **IoT Device** | ESP32 / Arduino with GPS Module |
| **Database** | Firebase Realtime Database / SQLite |
| **Cloud Services** | Firebase Cloud Messaging, Google Maps API |

---

## ⚙️ System Architecture

[User/IoT Device]
|
v
[Mobile App] ---> [Firebase Cloud] ---> [Emergency Contacts]
|
v
[Location Tracking + Proof Storage]

---

## 🔌 Hardware Components

| Component | Quantity | Description |
|------------|-----------|-------------|
| ESP32 / Arduino | 1 | Main controller for IoT |
| GPS Module | 1 | Tracks real-time location |
| Push Button | 1 | Triggers SOS alert |
| Battery | 1 | Portable power source |
| Wi-Fi Module | Built-in (ESP32) | Connects device to Firebase |

---

## 📱 Mobile App Interface

- 🧍 User registration & login  
- 🚨 SOS activation  
- 🗺️ Real-time location map  
- 📞 Contact notifications  
- 💬 Feedback & report section  

<p align="center">
  <img src="https://media.giphy.com/media/3ohzdQ1IynzclJldUQ/giphy.gif" width="250"/>
</p>

---

## 🔥 Firebase Database Structure

```json
{
  "user": {
    "uid": "12345",
    "name": "Jane Doe",
    "location": "7.2906, 80.6337",
    "status": "SOS Active",
    "contacts": {
      "1": "077XXXXXXX",
      "2": "071XXXXXXX"
    }
  },
  "alerts": {
    "active": true,
    "timestamp": "2025-09-15T18:23:45Z"
  }
}
