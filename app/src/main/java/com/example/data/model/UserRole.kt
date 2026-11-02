package com.example.data.model

enum class UserRole(val label: String, val deskripsi: String) {
  ADMIN("Admin BOS", "Kelola kegiatan, anggaran, & pengaturan sistem"),
  PETUGAS("Petugas Lapangan", "Dokumentasi foto kamera langsung & bukti fisik"),
  PIMPINAN("Kepala Sekolah / Pengawas", "Pemeriksaan keabsahan bukti & cetak LPJ")
}

data class UserSession(
  val userId: String = "user_smkn3_01",
  val displayName: String = "Jaya Pinuyut, S.Pd",
  val email: String = "jaya.pinuyut@gmail.com",
  val role: UserRole = UserRole.PETUGAS,
  val schoolName: String = "SMK NEGERI 3 KOTAMOBAGU"
)
