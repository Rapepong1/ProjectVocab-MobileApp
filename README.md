# 📱 Mobile App CP213

> **Practice English Vocab (เหมือนคำศัพท์ Flashcard)**

ฝึกภาษาอะไรก็ได้
สามารถแบ่ง set หมวดหมู่ได้ เช่น คำศัพท์อังกฤษ, คำศัพท์ญี่ปุ่น, คำศัพท์อังกฤษประเภทอาหาร, คำศัพท์อังกฤษประเภทการเงิน

---

# Requirement

## 🚀 Features
- ให้ user Input คำศัพท์ที่ตัวเองอยากรู้ได้ (มี starter pack ให้)
- Delete คำศัพท์, Delete หมวดหมู่คำศัพท์, Add คำศัพท์, Add หมวดหมู่คำศัพท์, Edit คำศัพท์, Edit หมวดหมู่คำศัพท์
- มี Quiz ให้ User สามารถเล่นได้ โดยจะสุ่มคำศัพท์ที่มีอยู่ในคลังที่ Input ไป จะสุ่มและมีปุ่มกด Back, Reveal, Next คำศัพท์จะสุ่มทุกครั้งที่เริ่ม Quiz ใหม่
- Sorted คำศัพท์ได้ เช่น ตามลำดับที่ add, A-Z
- ปุ่ม Shuffle คำศัพท์
- สามารถกด Favorite คำศัพท์ได้
- คำที่ Favorite จะอยู่ด้านบนสุดเสมอ ตอน Quiz ก็จะบอกว่า คำๆนี้เรา Favorite ไว้
- **รองรับการแสดงผลทั้ง Dark Mode และ Light Mode**
- **มี Widget บนหน้า Home Screen แสดงคำศัพท์และคำแปล (ระบบจะ Refresh คำศัพท์ใหม่ทุกวัน และมีปุ่มให้กด Refresh เองบน Widget ได้)**

<br>

<div align="center">
  <img src="Source Code/docs/image-2.png" alt="alt text" width="45%">
  &nbsp;
  <img src="Source Code/docs/image-1.png" alt="alt text" width="40%">
</div>

<br>

---

## 📝 Input Word
- คำศัพท์, Part-of-Speech (POS) (optional), คำแปล

## 🎨 UI/UX
เข้า app จะเป็นหน้า All Words และมีปุ่มให้ switch ระหว่าง หน้าคำศัพท์และ Quiz

## 💡 Optional
- กดคำศัพท์แล้วจะเข้าไป search คำๆนั้นใน Dictionary (In-App WebView)

<br><hr>

# Project Details

แอปพลิเคชันสำหรับจัดการและฝึกฝนคำศัพท์ภาษาอังกฤษบนระบบปฏิบัติการ Android ออกแบบมาให้ใช้งานง่าย ทันสมัย เน้นการสร้างคลังคำศัพท์ส่วนตัว และพัฒนาความจำผ่านระบบ Quiz และพจนานุกรมในตัว

## 📖 1. Project Overview
โปรเจกต์นี้พัฒนาขึ้นเพื่อแก้ปัญหาการจดจำคำศัพท์ โดยผู้ใช้สามารถบันทึกคำศัพท์ใหม่ แยกตามหมวดหมู่ (Categories) ระบุคำแปลและประเภทของคำ (Part of Speech) ได้ด้วยตัวเอง นอกจากนี้ยังมีระบบ Quiz แบบสุ่มเพื่อทดสอบความจำ สามารถดูความหมายเพิ่มเติมจากพจนานุกรมออนไลน์ได้ทันทีโดยไม่ต้องสลับแอปพลิเคชัน และมาพร้อมกับ Widget บนหน้าจอหลักเพื่อการเรียนรู้ที่เข้าถึงได้ตลอดเวลา

<br>

## 🛠 2. Tech Stack
โปรเจกต์นี้ใช้ Modern Android Development (MAD) skills ในการพัฒนา:
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Declarative UI)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Data Storage:** SharedPreferences + JSON (Local Persistence)
* **State Management:** Kotlin Coroutines & StateFlow

<br>

## ✨ 3. Key Features

### 🔍 Word Peek & In-App Dictionary
* **Long Press Gesture:** ใช้ `pointerInput` และ `detectTapGestures` ดักจับการกดค้างที่การ์ดคำศัพท์เพื่อเปิด `WordPeekCard`
* **Seamless WebView:** นำคำศัพท์ไปสร้าง URL ค้นหาใน Cambridge Dictionary และแสดงผลผ่าน `AndroidView` (WebView) ภายในแอปทันที

### 💾 Data Persistence
* **JSON Manager:** ระบบแปลง Object คำศัพท์และหมวดหมู่เป็นรูปแบบ JSON (ผ่าน `JSONArray` และ `JSONObject`)
* **Auto-Load State:** บันทึกข้อมูลลง `SharedPreferences` ถาวร และโหลดกลับมาอัปเดตผ่าน `MainViewModel` ทุกครั้งที่เปิดแอป

### 🎮 Interactive Quiz Mode
* **Smart Shuffle:** สุ่มลำดับคำศัพท์ใหม่ทุกครั้งที่เริ่มทำแบบทดสอบ
* **Hint & Reveal Animation:** ระบบคำใบ้ (แสดงอักษรตัวแรก) และแตะเพื่อดูเฉลย โดยใช้ระบบ Animation ของ Compose เพื่อความลื่นไหล

### 🌗 Dark / Light Mode Support
* **Dynamic Theme:** รองรับการเปลี่ยนธีมตามการตั้งค่าของระบบ (System Settings) เพื่อความสบายตาในการใช้งานในทุกสภาพแสง

### 📱 Home Screen Widget
* **Daily Vocab:** วิดเจ็ตบนหน้าจอโฮมที่ดึงคำศัพท์จากคลังของผู้ใช้มาแสดงผล
* **Auto & Manual Refresh:** ระบบอัปเดตคำศัพท์ใหม่ให้อัตโนมัติทุกวัน และมีปุ่ม Refresh บนตัว Widget ให้ผู้ใช้กดสุ่มคำศัพท์ใหม่ได้ทันที

<br>

## 📂 4. Project Structure
การจัดระเบียบไฟล์และองค์ประกอบสำคัญภายในโปรเจกต์:
* `MainActivity.kt`: หัวใจหลักของ UI จัดการ Navigation (Tab), หน้าจอรายการคำศัพท์ (WordList), หน้าจอทดสอบ (Quiz) และ Dialog ทั้งหมดด้วย Jetpack Compose
* `MainViewModel.kt`: จัดการ Business Logic ทั้งหมด รวมถึงการประมวลผลคำศัพท์ใน Quiz, การกรองหมวดหมู่ (Filter), การเรียงลำดับ (Sort) และสถานะ Dark Mode
* `Models.kt`: กำหนดโครงสร้างข้อมูลพื้นฐาน (Data Classes) ได้แก่ `Word` (คำศัพท์) และ `Category` (หมวดหมู่)
* `JsonManager.kt`: (DataManager) จัดการการจัดเก็บข้อมูลแบบ Local Persistent โดยใช้ SharedPreferences และแปลงข้อมูลเป็น JSON
* `VocabularyWidget.kt`: ควบคุมการทำงานของ Home Screen Widget รวมถึงการสุ่มคำศัพท์ประจำวัน (Daily Update) และระบบ Shuffle
* `ui.theme/`: เก็บการตั้งค่าธีม สี และตัวอักษรของแอป (Material 3) เพื่อให้รองรับทั้ง Light และ Dark Mode
* `res/layout/`: เก็บไฟล์ XML Layout สำหรับหน้าตาของ Home Screen Widget (`vocabulary_widget.xml`)
* `res/xml/`: เก็บไฟล์คอนฟิกูเรชันสำหรับ App Widget Provider ความละเอียดและระยะเวลาอัปเดตต่างๆ

<br><hr>

### 🎥 Vocabulary Mobile Application - Showcase Video

<div align="center">
  <a href="https://youtu.be/h61tATwW6JY">
    <img src="https://img.youtube.com/vi/h61tATwW6JY/0.jpg" width="600" alt="Watch the video">
  </a>
  <br>
  <em> 🔗 คลิกที่รูปเพื่อรับชม Vocab App Video 🔗</em>
</div>
<br>
