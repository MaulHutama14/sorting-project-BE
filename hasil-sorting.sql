SELECT tpk.sort_id,tpk.assign_date as start,tpk.assign_end AS end , tpk.nama_komponen, tk.master_nama_produk, ta.nama_alat, tp.nama_proses, tp.sort_id AS urutan_proses 
FROM tx_proses_komponen tpk
JOIN tx_komponen tk ON tk.nama_komponen=tpk.nama_komponen
JOIN tm_proses tp ON tp.id=tpk.id_proses
JOIN tm_alat ta ON ta.nama_alat=tpk.nama_id_alat
WHERE tpk.assign_date >'2020-04-01' AND tpk.assign_end < '2020-05-31'
ORDER BY tpk.sort_id ASC, tp.sort_id ASC ; -- PAKAI BATAS

SELECT tpk.sort_id,tpk.assign_date as START,tpk.assign_end AS end , tpk.nama_komponen, tk.master_nama_produk, ta.nama_alat, tp.nama_proses, tp.sort_id AS urutan_proses 
FROM tx_proses_komponen tpk
JOIN tx_komponen tk ON tk.nama_komponen=tpk.nama_komponen
JOIN tm_proses tp ON tp.id=tpk.id_proses
JOIN tm_alat ta ON ta.nama_alat=tpk.nama_id_alat
ORDER BY tpk.sort_id ASC, tp.sort_id ASC ; -- SEMUA