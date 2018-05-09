## WiMa Studienprojekt 2018
# Online Produktions- und Lagerhaltungsproblem

### Problembeschreibung

Mehrperiodische Betrachtung der Produktions- und Lagerhaltungsentscheidungen eines Unternehmens unter Unsicherheit.
* Startkapital `K`
* `n` Typen von Produkten. Jedes Produkt `i` ist charakterisiert durch
  * Produktionskosten `c_i`
  * Verkaufspreis `p_i`
  * Volumen `v_i`
  * Wegwerfkosten `w_i`
  * Produktionsbeschränkung `b_i`
  * (Nachfrage `d_i`)
* Bankrott bei negativem Kapital

Entscheidungsablauf in Periode `j`:
* Fixkosten `F` werden bezahlt
* Entscheidung wie viel von jedem Produkt in Periode `j` produziert werden soll
* Dazu: eingeschränkte Kenntnis über Nachfrage `d_i`(3 Möglichkeiten)
  1. Nachfrage von Anfang an bekannt
  2. Zufallsvariable, anhand der die Nachfrage verteilt ist, ist bekannt
  3. keine weitere Kenntnis als die aus vorherigen Perioden (`0,...,j-1`)gesammelten Informationen
* Tatsaächliche Nachfrage `d_i` wird bekannt
* Bereits produzierte Produkte werden entsprechend der Nachfrage verkauft
* Übrig bleibende Produkte müssen weggeworfen oder zwischengelagert werden
  1. Entscheidung wie viel von jedem Produkt weggeworfen wird
  2. Entscheidung wie viele Lagerräume zur Zwischenlagerung angemietet werden
  3. Entscheidung welches Produkt in welchem Lagerraum gelagert wird

Zur Zwischenlagerung stehen Lagerräume zur Verfügung, die angemietet werden können.
* Lagervolumen `V`
* Lagerkosten je Periode `C`

Zielsetzung
* Maximierung des Kapitals
* Bankrott vermeiden


### Format der Instanzen

Die statischen Problemparameter sind in einer Textdatei `instance_name.in` von ASCII-Zeichen mit Unix-Zeilenenden.
In der ersten Zeile sind gegeben: das Startkapital `K`, die Fixkosten je Periode `F`, die Kosten für das Anmieten eines Lagerraums `C`, das Volumen eines Lagerraums `V` sowie die Anzahl der Produkte `n`. Danach folgt für jedes Produkt eine Zeile mit Produktionskosten `c_i`, Verkaufspreis `p_i`, Volumen `v_i`, Wegwerfkosten `w_i` und Produktionsschranke `b_i`.

```
S F C V n
c_1 p_1 v_1 w_1 b_1
c_2 p_2 v_2 w_2 b_2
...
c_n p_n v_n w_n b_n
```


Die nicht-statischen Parameter (Nachfrage) sind in einer weitern Textdatei `instance_name.dem` gegeben. In der ersten Zeile steht die Anzahl der Perioden `m`. Danach folgt für jede Periode `j` eine Zeile mit der Nachfrage `d_(j,1),...,d_(j,n)` für jeden Produkttyp.
```
m
d_(1,1) d_(1,2) ... d_(1,n)
d_(2,1) d_(2,2) ... d_(2,n)
...
d_(m,1) d_(m,2) ... d_(m,n)
```
