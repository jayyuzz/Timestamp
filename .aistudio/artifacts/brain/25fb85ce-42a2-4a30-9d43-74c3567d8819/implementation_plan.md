# BOS Cam — Dokumentasi Real-Time & Pelaporan Dana BOS SMK Negeri 3 Kotamobagu

Aplikasi Android native mobile-first untuk verifikasi dan pelaporan dokumentasi kegiatan belanja Bantuan Operasional Sekolah (BOS) SMK Negeri 3 Kotamobagu secara real-time, anti-manipulasi (khusus kamera langsung, tanpa unggah galeri), dilengkapi watermark identitas resmi (logo, WITA, GPS, komponen BOS, nomor bukti), hash kriptografi SHA-256, serta generator laporan PDF resmi dan arsip ZIP.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Ringkasan keputusan yang telah dikonfirmasi dan direkomendasikan bersama pengguna:

- **Confirmed Decision 1 (Arsitektur)**: Menggunakan **Native Android (Kotlin & Jetpack Compose)** untuk memastikan performa kamera real-time mulus, pembatasan ketat pengambilan gambar kamera langsung tanpa galeri, akses GPS/WITA presisi, serta rendering watermark dan generator PDF lokal tanpa dependensi server eksternal.
- **Confirmed Decision 2 (Data & Penyimpanan)**: **Arsitektur Hybrid Offline-First** dengan database lokal Room untuk antrean dokumentasi instan di lapangan, penyimpanan file foto lokal ber-watermark, serta antrean sinkronisasi background cloud/Firestore saat koneksi internet tersedia.
- **Confirmed Decision 3 (Autentikasi & Hak Akses)**: **Multi-Peran (Admin, Petugas, Pimpinan)** dengan dukungan Google Sign-In terintegrasi serta selector peran cepat (Fast Role Switcher) untuk kemudahan operasional dan simulasi audit.

---

## 1. Overview & Core Concept

- **Apa itu BOS Cam**: Aplikasi instrumen kepatuhan dan pelaporan belanja dana BOS pada SMK Negeri 3 Kotamobagu. Aplikasi menjamin setiap foto belanja barang/jasa benar-benar diambil di lokasi riil pada waktu pelaksanaan sebenarnya.
- **Sasaran Pengguna**:
  - **Petugas Lapangan / Guru / Tim Pelaksana BOS**: Memilih kegiatan, menentukan jenis dokumentasi (Sebelum, Proses, Sesudah, Barang, Nota, Serah Terima), memotret langsung dengan panduan kamera, melihat pratinjau watermark, dan menyimpan ke antrean offline.
  - **Admin BOS / Bendahara Sekolah**: Mengelola daftar kegiatan, alokasi komponen BOS, memantau antrean sinkronisasi, dan mengekspor arsip data/ZIP.
  - **Pimpinan (Kepala Sekolah & Tim Pengawas/Auditor)**: Memeriksa validitas arsip foto dengan hash SHA-256, meninjau status belanja, dan mengunduh/mencetak Laporan Pertanggungjawaban (LPJ) PDF lengkap berkop sekolah dan kolom pengesahan tanda tangan.
- **Nilai Kunci**:
  - **Anti-Fraud & Validitas Hukum**: Hanya menerima jepretan kamera langsung (*direct hardware camera capture*), menolak akses file picker/galeri, menghitung checksum SHA-256 pada setiap foto asli dan foto berwatermark.
  - **Watermark Komprehensif Berstandar BOS**: Logo SMK Negeri 3 Kotamobagu, stempel waktu WITA (UTC+8), titik koordinat lintang/bujur dan nama jalan/kelurahan Kotamobagu, nama kegiatan, kode komponen BOS, nama petugas, dan nomor register unik otomatis.
  - **Ergonomi Layar 360dp**: Tata letak ramah jempol satu tangan dengan tombol minimal 48dp, kontras tinggi, dan umpan balik haptik/visual instan.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Autentikasi & Pemilihan Peran**:
   - Pengguna masuk dengan akun resmi; terdapat lencana identitas pengguna aktif dan saklar cepat peran (Admin, Petugas, Pimpinan).
2. **Alur Kerja Pemotretan Petugas**:
   - Petugas memilih Kegiatan BOS aktif (misal: "Pengadaan Alat Praktik Jurusan TKJ - Komponen 03").
   - Petugas memilih Tahapan Foto dari 6 kategori wajib:
     - 1. *Sebelum (0%)*
     - 2. *Proses (50%)*
     - 3. *Sesudah (100%)*
     - 4. *Fisik Barang*
     - 5. *Nota / Faktur Pembelian*
     - 6. *Berita Acara Serah Terima*
   - Membuka antarmuka kamera khusus (CameraX) dengan grid pemandu, status penguncian GPS aktif, dan waktu WITA real-time.
   - Mengambil foto -> aplikasi seketika me-render watermark resmi beresolusi tinggi di atas kanvas citra -> menghitung hash SHA-256 -> menampilkan dialog pratinjau bukti sebelum disimpan.
3. **Penyimpanan & Antrean Sinkronisasi (Offline Queue)**:
   - Jika offline, data otomatis tersimpan di antrean Room lokal bertanda "Menunggu Sinkronisasi".
   - Indikator status jaringan real-time di bilah atas aplikasi dengan tombol sinkronisasi manual atau otomatis saat online.
4. **Arsip Galeri & Verifikasi Bukti**:
   - Galeri foto kartu visual dengan filter tanggal (kalender), filter kegiatan BOS, dan filter nama petugas.
   - Lembar detail menampilkan foto ber-watermark, kode verifikasi SHA-256, titik koordinat peta, dan histori unggahan.
5. **Pusat Ekspor Laporan PDF & ZIP**:
   - Pembuatan dokumen PDF resmi berformat standar pemerintah: Kop SMK Negeri 3 Kotamobagu (Provinsi Sulawesi Utara), tabel rincian kegiatan, matriks visual foto bukti belanja, serta lembar pengesahan tanda tangan Kepala Sekolah & Bendahara BOS.
   - Fitur kompresi ZIP untuk mengunduh seluruh berkas foto kegiatan dalam satu arsip terstruktur.

### Visual Identity & Theme (Material 3)

- **Arah Estetika**: *Official Institutional & High-Utility Field Camera* — kombinasi nuansa biru dongker kebangsaan sekolah vokasi (`Deep Navy #0D2B45`) dipadukan dengan aksen emas presisi (`Gold Ochre #FFB703`) dan latar belakang terang bersih yang ramah di luar ruangan.
- **Palet Warna**:
  - `Primary`: `#0F3A5D` (Navy Biru Resmi Pendidikan)
  - `OnPrimary`: `#FFFFFF`
  - `Secondary`: `#1D7874` (Teal Kerapian Verifikasi)
  - `Tertiary / Accent`: `#F59E0B` (Kuning Emas Penanda Status & Lencana)
  - `Background`: `#F8FAFC` (Slate Super Cerah untuk keterbacaan di bawah sinar matahari)
  - `Surface`: `#FFFFFF` dengan elevasi halus dan kontur kartu tegas
  - `Error / Reject`: `#DC2626` (Merah peringatan penolakan manipulasi)
- **Tipografi**:
  - Display & Judul: Headline tegas, tebal, dan formal untuk kop sekolah dan judul kegiatan.
  - Data Kripto & Koordinat: Monospace terstruktur untuk Hash SHA-256 dan koordinat GPS agar mudah dibaca auditor.
  - Body: Ukuran minimal 14sp dengan kontras tajam yang nyaman dibaca di layar kompak 360dp.
- **Komponen & Ergonomi 360dp**:
  - Tombol aksi utama (Shutter Kamera, Simpan Bukti, Filter Cepat) berukuran minimal 52dp dengan jari-jari sudut membulat 16dp.
  - Penguncian orientasi dan penempatan navigasi di bagian bawah (Bottom App Bar) yang mudah dijangkau satu tangan petugas di lapangan.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native CameraX Khusus Tanpa File Picker Galeri**:
  - *Pendekatan*: Aplikasi hanya membuka hardware kamera langsung via Android CameraX / ActivityResultContracts.TakePicture. Tidak menyediakan opsi "Pilih dari Galeri".
  - *Alasan*: Kebijakan integritas audit dana BOS mengharuskan foto diambil seketika di tempat kejadian tanpa kemungkinan mengunggah foto lama atau hasil suntingan eksternal.
- **Decision 2: Pemrosesan Watermark dan SHA-256 Langsung di Perangkat (On-Device)**:
  - *Pendekatan*: Citra bitmap diproses melalui Android Graphics `Canvas` untuk menyematkan pita legalitas lengkap (Logo SMK, teks resmi, stempel WITA, alamat dan koordinat GPS) kemudian di-encode ke JPEG kualitas tinggi dan dihitung hash SHA-256 via `java.security.MessageDigest`.
  - *Alasan*: Cepat, instan, dapat berfungsi 100% tanpa sinyal internet di daerah sekolah atau toko belanja, dan menghasilkan file bukti sah yang langsung siap dibagikan.
- **Decision 3: Pembuat PDF & ZIP Mandiri di Sisi Android**:
  - *Pendekatan*: Menggunakan Android `PdfDocument` untuk menggambar format laporan LPJ bertanda tangan digital dan `java.util.zip.ZipOutputStream` untuk bundel ZIP.
  - *Alasan*: Menghilangkan kebutuhan server eksternal Node.js yang rentan mati atau memerlukan biaya hosting tambahan, memberikan kemandirian penuh bagi pihak sekolah saat audit offline.
- **Decision 4: Dukungan Mode Demonstrasi & Pengujian Cepat**:
  - *Pendekatan*: Menyertakan data benih (seed activities) komponen BOS reguler 2026 dan kemampuan beralih peran (Admin, Petugas, Pimpinan) dengan sekali sentuh agar evaluator dapat menguji seluruh alur kerja seketika.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        BOS CAM (ANDROID COMPOSE)                       │
├────────────────────────────────────────────────────────────────────────┤
│  UI Screens & Flow (Ergonomi 360dp)                                    │
│  ├─ Beranda & Ringkasan BOS (Statistik, Kegiatan Aktif, Status Sync)   │
│  ├─ Kamera Langsung (CameraX / Direct Capture - No Gallery Picker)     │
│  ├─ Pratinjau Watermark & Pemeriksa Integritas Hash SHA-256            │
│  ├─ Galeri Arsip Dokumentasi (Filter: Tanggal, Kegiatan, Petugas)     │
│  ├─ Manajemen Kegiatan & Komponen BOS (Admin)                          │
│  └─ Generator Dokumen (Cetak PDF Kop Sekolah & Ekspor Arsip ZIP)      │
├────────────────────────────────────────────────────────────────────────┤
│  Domain & Core Engines                                                 │
│  ├─ WatermarkStamper: Menempelkan Logo, WITA Time, GPS, Alamat, Info   │
│  ├─ HashVerifier: Menghitung SHA-256 Checksum pada file hasil jepret   │
│  ├─ LocationProvider: Mengambil Lat/Lng & Geocoding Kota Kotamobagu   │
│  ├─ PdfReportGenerator: Android PdfDocument (Kop Resmi + TTD Pejabat) │
│  └─ ZipExporter: Mengompres berkas bukti ke dalam file .zip terstruktur│
├────────────────────────────────────────────────────────────────────────┤
│  Data Layer & Persistence                                              │
│  ├─ Room Database (Offline Queue, Kegiatan BOS, Riwayat Dokumentasi)   │
│  ├─ Local Storage (Folder internal terisolasi untuk foto watermark)    │
│  └─ SyncManager (Menangani sinkronisasi otomatis saat online)          │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Models & Entities (Room & Domain)

1. **BosActivityEntity (Kegiatan BOS)**:
   - `id`: String / UUID
   - `kodeKegiatan`: String (contoh: "BOS-2026-001")
   - `namaKegiatan`: String (contoh: "Pemeliharaan Sarana Lab Komputer SMK")
   - `komponenBos`: String (contoh: "03. Pemeliharaan Sarana dan Prasarana")
   - `alokasiDana`: Double
   - `tahunAnggaran`: String ("2026")
   - `status`: String ("Aktif" / "Selesai")

2. **BosDocumentationEntity (Bukti Foto Dokumentasi)**:
   - `id`: String / UUID
   - `nomorDokumentasi`: String (Format otomatis: `SMKN3-BOS/2026/03/001`)
   - `activityId`: String (Relasi ke BosActivityEntity)
   - `activityName`: String
   - `komponenBos`: String
   - `category`: String ("Sebelum", "Proses", "Sesudah", "Barang", "Nota", "Serah Terima")
   - `officerName`: String
   - `officerRole`: String
   - `witaTimestamp`: Long & Format String (Waktu server WITA UTC+8)
   - `latitude`: Double
   - `longitude`: Double
   - `addressText`: String (contoh: "SMKN 3 Kotamobagu, Jl. AKD, Kotamobagu")
   - `photoFilePath`: String (Path file lokal ber-watermark)
   - `sha256Hash`: String (Checksum keaslian bukti 64 karakter heksadesimal)
   - `syncStatus`: String ("QUEUED", "SYNCED", "FAILED")

3. **UserSession**:
   - `userId`: String
   - `displayName`: String (contoh: "Jaya Pinuyut")
   - `email`: String
   - `role`: BosRole (`ADMIN`, `PETUGAS`, `PIMPINAN`)

### Interactive Component & State Mapping

- **Direct Shutter Action**: Menekan tombol rana -> mengecek izin kamera & lokasi GPS -> mengunci koordinat dan waktu WITA -> menangkap frame bitmap -> melewatkan ke `WatermarkStamper` -> menghitung SHA-256 -> membuka `WatermarkPreviewDialog` -> simpan ke Room dan antrean sinkronisasi.
- **Filter Bar Galeri**: Memilih tanggal dari modal DatePicker, memilih dropdown kegiatan, atau memilih petugas -> state Flow di ViewModel bereaksi otomatis memfilter daftar dokumentasi tanpa delay.
- **Ekspor Laporan PDF**: Memilih kegiatan target -> klik "Cetak LPJ PDF" -> `PdfReportGenerator` menyusun kop sekolah SMK Negeri 3 Kotamobagu, tabel foto Sebelum/Proses/Sesudah/Nota, dan kotak tanda tangan Kepala Sekolah & Bendahara -> membuka File Viewer atau Share Intent Android.
