Včelnice
Včelnice je inteligentní systém pro správu včelích úlů, který kombinuje moderní metody včelaření s technologií IoT. Systém zahrnuje Android aplikaci propojenou s ESP32 moduly, které sledují stav včelstev, sbírají data ze senzorů a poskytují přehledné statistiky.

📋 Základní funkce
Správa stanovišť a úlů:
- Přidávání a správa stanovišť a úlů. DONE
- Evidence poznámek, historie a aktuálního stavu včelstev. TODO
Sběr a analýza dat:
- Data získaná ze senzorů (teplota, vlhkost, váha, akcelerace). TODO
- Diagnostika zdraví včelstva na základě frekvencí zvuků (akustická analýza). TODO
Synchronizace dat:
- Synchronizace dat mezi aplikací a ESP32 pomocí BLE a WiFi.
- Automatické ukládání naměřených dat do Room databáze.
Přehledné vizualizace:
- Grafy pro zobrazení historických dat. TODO
- Hodnocení úlů pomocí jednoduchého hvězdičkového systému. TODO
Upozornění a notifikace:
- Monitoring problémů a upozornění na změny stavu. TODO

🛠️ Použité technologie
Android aplikace
- Programovací jazyk: Kotlin
- Databáze: Room
- Framework: MVVM s podporou LiveData
- UI komponenty: RecyclerView, NavigationView, SimpleRatingBar
- Knihovny:
  -- Dagger/Hilt pro Dependency Injection
  -- BluetoothGatt pro BLE komunikaci
  -- Grafické knihovny (např. MPAndroidChart)
ESP32 moduly centrální jednotky
- Hardware:
  -- ESP32 ProS3 od Unexpected Maker
  -- MicroSD karta a RTC modul pro ukládání a časovou synchronizaci dat
- Programovací jazyk: C++
- Komunikační protokoly: BLE, WiFi

ESP32 moduly úlové jednotky
- Hardware:
  -- ESP32 ProS3 od Unexpected Maker
  -- Senzory (teplota, vlhkost, váha, akcelerometr, mikrofon)
- Programovací jazyk: C++
- Komunikační protokoly: BLE, WiFi

Tento projekt je dostupný pod licencí MIT.
