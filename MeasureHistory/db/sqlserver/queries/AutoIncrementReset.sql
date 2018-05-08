-- Autowert einer Tabelle wieder auf 0 setzen
DBCC CHECKIDENT('Tabellenname', RESEED, 0)