## WiMa Studienprojekt 2018
# Ein Produktions- und Lagerhaltungsproblem unter unsicherer Nachfrage

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
* Tatsächliche Nachfrage `d_i` wird bekannt
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

Die statischen Parameter für die Nachfrage ist in einer Textdatei `instance_name.deminfo` gegeben. In der ersten Zeilde steht die Anzahl der Produkte `n`. Danach folgt für jedes Produkt `i` eine Zeile mit dem Erwartungswert und der Varianz der Nachfrage für dieses Produkt.

```
n
ew_1 var_1
ew_2 var_2
...
ew_n var_n
```

Die nicht-statischen Parameter (Nachfrage) sind in einer weiteren Textdatei `instance_name.dem` gegeben. In der ersten Zeile steht die Anzahl der Perioden `m`. Danach folgt für jede Periode `j` eine Zeile mit der Nachfrage `d_(j,1),...,d_(j,n)` für jeden Produkttyp.
```
m
d_(1,1) d_(1,2) ... d_(1,n)
d_(2,1) d_(2,2) ... d_(2,n)
...
d_(m,1) d_(m,2) ... d_(m,n)
```


### Kommunikation mit dem Server

Zum Abschluss des Studienprojekts gibt es einen Wettbewerb, bei dem alle Teams gegeneinander antreten. Dazu wird ein Server bereitgestellt, der die Instanz an alle Teams ausgibt. Dieser Server überwacht auch, dass alle Teams zulässige Entscheidungen treffen. Alle Zahlen werden als 32-bit Integerzahlen übermittelt. Der Ablauf der Kommunikation zwischen den einzelnen Teams und dem Server während des Wettbewerbs ist wie folgt:

* Server wird gestartet
* Teams verbinden sich mit dem Server (Socket erstellen)
    * Teamnamen werden an den Server geschickt (String)
* Teams warten auf Grunddaten der Instanz
    * Startkapital `S`
    * Anzahl der Perioden `m`
    * Anzahl der Produkte `n`
    * Volumen für ein Lager `V`
    * Kosten für ein Lager `C`
    * Fixkosten pro Periode `F`
    * Produktionsschranken `b_1, b_2, ..., b_n`
    * Herstellungskosten `c_1, c_2, ..., c_n`
    * Verkaufserlöse `p_1, p_2, ..., p_n`
    * Entsorgungskosten `w_1, w_2, ..., w_n`
    * Volumen `v_1, v_2, ..., v_n`
    * Erwartungswerte `ew_1, ew_2, ..., ew_n`
    * Varianzen `var_1, var_2, ..., var_n`
* 30 Sekunden für die Entscheidungen der erste Periode laufen
* Teams schicken Produktionsentscheidungen `x_1, x_2, ..., x_n` (innerhalb der 30 Sekunden)
* Es wird vom Server überprüft ob alle Entscheidungen zulässig sind
* Es wird vom Server überprüft ob ein Team negatives Kapital hat -> Bankrott
* Server sendet an Teams zurück:
    * neues Kapital
    * Überschuss an Produkten `a_1, a_2, ..., a_n`
* 30 Sekunden für die Entscheidungen der nächsten Periode laufen
* Teams schicken Lagerentscheidung
    * Anzahl verwendeter Lagerräume `L`
    * für jedes Lager: Lagermengen der Produkte `l_(k,1), l_(k,2), ..., l(k,n)`
* Teams schicken Produktionsentscheidungen `x_1, x_2, ..., x_n` (innerhalb der 30 Sekunden)
* Es wird vom Server überprüft ob alle Entscheidungen zulässig sind
* Es wird vom Server überprüft ob ein Team negatives Kapital hat -> Bankrott
...
