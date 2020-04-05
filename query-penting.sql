SELECT tpk.nama_komponen, tk.prioritas, tk.durasi_pengerjaan, COUNT(*) AS jumlah_proses FROM tx_proses_komponen tpk
JOIN tm_proses tp ON tp.id=tpk.id_proses
JOIN tm_master_proses tmp ON tmp.nama_proses=tp.nama_m_proses
JOIN tx_komponen tk ON tk.nama_komponen=tpk.nama_komponen
JOIN tx_produk tpd ON tpd.nama_produk=tk.master_nama_produk
 GROUP BY tpk.nama_komponen
ORDER BY tp.sort_id ASC, tpd.tanggal_akhir ASC, tk.prioritas ASC, tk.durasi_pengerjaan DESC, COUNT(*) DESC, tk.nama_komponen ASC;

SELECT tpk.* FROM tx_proses_komponen tpk
		JOIN (SELECT tpk.nama_komponen AS nama FROM tx_proses_komponen tpk
			JOIN tm_proses tp ON tp.id=tpk.id_proses
			JOIN tm_master_proses tmp ON tmp.nama_proses=tp.nama_m_proses
			JOIN tx_komponen tk ON tk.nama_komponen=tpk.nama_komponen
			JOIN tx_produk tpd ON tpd.nama_produk=tk.master_nama_produk
			 GROUP BY tpk.nama_komponen
			ORDER BY tpd.tanggal_akhir ASC, tk.prioritas ASC, tk.durasi_pengerjaan DESC, COUNT(*) DESC, tk.nama_komponen ASC) AS ask
		ON ask.nama=tpk.nama_komponen
		JOIN tm_proses tp ON tp.id=tpk.id_proses
		ORDER BY tp.sort_id ASC;

SELECT tpk.id_proses, COUNT(*) AS jumlah_proses FROM tx_proses_komponen tpk
 GROUP BY tpk.id_proses
ORDER BY  COUNT(*) DESC;

INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'PLM','PLM','Cutting');

INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'ET','ET','Cutting');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'SGC','SGC','Cutting');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'BVL','BVL','Cutting');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'BS','BS','Cutting');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'HGC','HGC','Cutting');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'AGC','AGC','Cutting');


INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'STP','STP','FORMING');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'HPP','HPP','FORMING');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'BPB','BPB','FORMING');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'RB','RB','FORMING');

INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'RD','RD','MACHINING');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'TD','TD','MACHINING');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'HB','HB','MACHINING');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'GL','GL','MACHINING');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'LB','LB','MACHINING');
INSERT INTO `tm_proses` ( `id`, `nama_proses`, `nama_m_proses`) VALUES ( 'GR','GR','MACHINING');




spri_local