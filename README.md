# 📱 Mobile App CP213

> **Practice English Vocab (เหมือนคำศัพท์ Flashcard)**

ฝึกภาษาอะไรก็ได้
สามารถแบ่ง set หมวดหมู่ได้ เช่น คำศัพท์อังกฤษ, คำศัพท์ญี่ปุ่น, คำศัพท์อังกฤษประเภทอาหาร, คำศัพท์อังกฤษประเภทการเงิน

---

# Requirement

## 🚀 Features
- ให้ user Input คำศัพท์ที่ตัวเองอยากรู้ได้(มี starter pack ให้)
- Delete คำศัพท์, Deleteหมวดหมู่คำศัพท์, Add คำศัพท์, Add หมวดหมู่คำศัพท์, Edit คำศัพท์, Edit หมวดหมู่คำศัพท์
- มี Quiz ให้ User สามารถเล่นได้ โดยจะสุ่มคำศัพท์ที่มีอยู่ในคลังที่ Input ไป จะสุ่มและมีปุ่มกด Back, Reveal, Next คำศัพท์จะสุ่มทุกครั้งที่เริ่ม Quiz ใหม่
- Sorted คำศัพท์ได้ เช่น ตามลำดับที่ add, A-Z
- ปุ่ม Shuffle คำศัพท์
- สามารถกด Favorite คำศัพท์ได้
- คำที่ Favorite จะอยู่ด้านบนสุดเสมอ ตอน Quiz ก็จะบอกว่า คำๆนี้เรา Favorite ไว้

<br>

<div align="center">
  <img src="Source Code/docs/image-2.png" alt="alt text" width="50%">
  &nbsp;
  <img src="Source Code/docs/image-1.png" alt="alt text" width="45%">
</div>

<br>

---

## 📝 Input Word
- คำศัพท์, Part-of-Speech (POS)(optional), คำแปล

## 🎨 UI/UX
เข้า app จะเป็นหน้า All Words และมีปุ่มให้ switch ระหว่าง หน้าคำศัพท์และ Quiz

## 💡 optional
- กดคำศัพท์แล้วจะเข้าไป searh คำๆนั้นใน Dictionary

<br><hr>

# Project Details

แอปพลิเคชันสำหรับจัดการและฝึกฝนคำศัพท์ภาษาอังกฤษบนระบบปฏิบัติการ Android ออกแบบมาให้ใช้งานง่าย ทันสมัย เน้นการสร้างคลังคำศัพท์ส่วนตัว และพัฒนาความจำผ่านระบบ Quiz และพจนานุกรมในตัว

## 📖 1. Project Overview
โปรเจกต์นี้พัฒนาขึ้นเพื่อแก้ปัญหาการจดจำคำศัพท์ โดยผู้ใช้สามารถบันทึกคำศัพท์ใหม่ แยกตามหมวดหมู่ (Categories) ระบุคำแปลและประเภทของคำ (Part of Speech) ได้ด้วยตัวเอง นอกจากนี้ยังมีระบบ Quiz แบบสุ่มเพื่อทดสอบความจำ และสามารถดูความหมายเพิ่มเติมจากพจนานุกรมออนไลน์ได้ทันทีโดยไม่ต้องสลับแอปพลิเคชัน

<br>

## 🛠 2. Tech Stack
โปรเจกต์นี้ใช้ Modern Android Development (MAD) skills ในการพัฒนา:
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Declarative UI)
* **Architecture:** MVVM (Model-View-ViewModel) แยก Logic และ UI อย่างชัดเจน
* **Data Storage:** SharedPreferences + JSON (Local Persistence)
* **State Management:** Kotlin Coroutines & StateFlow สำหรับอัปเดต UI แบบ Real-time

<br>

## ✨ 3. Key Features

### 🔍 Word Peek & In-App Dictionary
* **Long Press Gesture:** ใช้ `pointerInput` และ `detectTapGestures` ดักจับการกดค้างที่การ์ดคำศัพท์เพื่อเปิด `WordPeekCard`
* **Seamless WebView:** นำคำศัพท์ไปสร้าง URL ค้นหาใน Cambridge Dictionary และแสดงผลผ่าน `AndroidView` (WebView) ภายในแอปทันที

### 💾 Data Persistence
* **JSON Manager:** ระบบแปลง Object คำศัพท์และหมวดหมู่เป็นรูปแบบ JSON (ผ่าน `JSONArray` และ `JSONObject`)
* **Auto-Load State:** บันทึกข้อมูลลง `SharedPreferences` ถาวร และโหลดกลับมาอัปเดตผ่าน `MainViewModel` ทุกครั้งที่เปิดแอป ข้อมูลไม่สูญหาย

### 🎮 Interactive Quiz Mode
* **Smart Shuffle:** สุ่มลำดับคำศัพท์ใหม่ทุกครั้งที่เริ่มทำแบบทดสอบ
* **Hint & Reveal Animation:** ระบบคำใบ้ (แสดงอักษรตัวแรก) และแตะเพื่อดูเฉลย โดยใช้ระบบ Animation ของ Compose เพื่อความลื่นไหลในการใช้งาน

<br>

## 📂 4. Project Structure
การจัดระเบียบไฟล์ภายในโปรเจกต์เน้นความสะอาดและดูแลรักษาง่าย:
* `MainActivity.kt`: จัดการ Navigation, UI Screens และ Dialogs
* `MainViewModel.kt`: ควบคุม Business Logic, การ Filter หมวดหมู่ และ Sort ข้อมูล
* `Models.kt`: Data classes สำหรับ `Word` และ `Category`
* `JsonManager.kt`: Core utility สำหรับจัดการเซฟและโหลดข้อมูล Local

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
