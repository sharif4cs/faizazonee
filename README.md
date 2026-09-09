# ফাইজা হিসাব (Faiza Hisab) - Smart Shop Management Android App

একটি আধুনিক, ১০০% অফলাইন ও ক্যাশিয়ার-বান্ধব দোকান ও ব্যবসা পরিচালনা করার অ্যান্ড্রয়েড অ্যাপ (Modern Shop & Ledger Management Android Application built with Jetpack Compose & Room Database).

---

## 📱 ডাউনলোড ও ইনস্টল (Download APK)
রেপোজিটরি থেকে সরাসরি ডাউনলোড করতে পারেন:
- **APK লোকেশন:** [`apk/faiza-hisab-v1.0.apk`](apk/faiza-hisab-v1.0.apk)

---

## ✨ মূল ফিচারসমূহ (Key Features)

1. **ড্যাশবোর্ড (Real-time Dashboard):**
   - দৈনিক, সাপ্তাহিক, মাসিক ও সামগ্রিক মোট বিক্রি, খরচ, নিট লাভ এবং ক্যাশ ব্যালেন্স ক্যালকুলেটর।
   - বাকি খাতার লাইভ পরিসংখ্যান ও কুইক অ্যাকশন মেনু।

2. **ইনভেন্টরি ও প্রোডাক্ট ক্যাটালগ (Inventory & Products):**
   - বারকোড স্ক্যানার ও কিউআর সাপোর্ট।
   - প্রোডাক্ট কেনা দাম, বিক্রয় দাম, সাইজ/ভ্যারিয়েন্ট এবং স্টক সতর্কবার্তা (Low Stock Alerts)।
   - নতুন প্রোডাক্ট যুক্ত, এডিট এবং ডিলিট করার সুবিধা।

3. **পয়েন্ট অব সেল (POS / Sale Counter):**
   - দ্রুত আইটেম কার্ট এবং মোট হিসাব।
   - ডিসকাউন্ট ক্যালকুলেশন।
   - ক্যাশ, বিকাশ ও বাকিতে বিক্রয় সুবিধা এবং সরাসরি ইনভয়েস জেনারেশন।

4. **বাকি খাতা ও কাস্টমার লেজার (Due & Customer Ledger):**
   - কাস্টমার ভিত্তিক সম্পূর্ণ খতিয়ান (ক্রয়, পরিশোধ ও বর্তমান বাকি)।
   - সরাসরি কল এবং এসএমএস রিমাইন্ডার পাঠানোর সুযোগ।

5. **খরচের খাতা (Daily Expense Tracker):**
   - ক্যাটাগরি অনুযায়ী দোকান ভাড়া, স্টাফ খরচ, নাস্তা, পরিবহন ও বিল হিসাব সংরক্ষণ।

6. **রিপোর্ট ও অ্যানালিটিক্স (Reports & Analytics):**
   - মাসিক ও বার্ষিক বিক্রয় ও লাভ-লোকসান বিশ্লেষণ।

7. **১০০% অফলাইন ডাটাবেজ (Offline First):**
   - ইন্টারনেট ছাড়াই রোম ডাটাবেজে (Room Database) সম্পূর্ণ ডেটা সুরক্ষিত থাকে।

---

## 🛠️ প্রযুক্তি (Tech Stack)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** Clean MVVM (Model-View-ViewModel) + Flow & Coroutines
- **Database:** Room Database (SQLite)
- **Build System:** Gradle Kotlin DSL

---

## 🚀 লোকাল মেশিনে বিল্ড করার নিয়ম
```bash
git clone <repo-url>
cd faiza-hisab
./gradlew assembleDebug
```
বিল্ড করা APK পাওয়া যাবে: `app/build/outputs/apk/debug/app-debug.apk`
