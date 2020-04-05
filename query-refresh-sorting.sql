UPDATE tx_proses_komponen tpk SET tpk.assign_date=NULL, tpk.assign_date_str=NULL, tpk.sort_id=NULL,
											tpk.assign_end=NULL, tpk.assign_end_str=NULL, tpk.nama_id_alat=NULL;
DELETE FROM master_tanggal_alat;