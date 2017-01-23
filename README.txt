TripleA Go AI (Wintersemester 2014/15)


DAS TEAM:

GO Group
Aigner Andreas (0800304)
Berger Christoph (1129111)
Wolfsberger Lukas (1100986)


INSTALLATION:

Siehe INSTALL.txt


BESCHREIBUNG:

Um die verschiedene Aufgaben wie das Durchführen der Simulationen und das Finden von geeigneten Zügen schnell und effizient erledigen zu können, haben wir unsere eigene Repräsentation des Go-Boards entwickelt. Zu Beginn der Suche wird der aktuelle Game-State von der Triple-A Datenstruktur ausgelesen und in unsere eigene Datenstruktur eingelesen. Alle weiteren Operationen erfolgen dann mit dieser internen Datenstruktur. Der Spielfortschritt (Anzahl und Position der Steine, miteinander verbundene Steine, Freiheiten der Steinketten, etc.) wird dabei jeweils inkrementell angepasst und gespeichert um anschließend möglichst effiziente Abfragen zu ermöglichen.

Für die eigentliche Suche des besten Zuges verwenden wir die Monte Carlo Tree Search. UCT wird verwendet um ein geeignetes Mittel zwischen Exploration und Exploitation zu finden. Für eine schnellere Evaluierung der Züge wenden wir außerdem RAVE mit einer angepassten Gewichtung an. Da Züge, welche erst sehr spät in der Simulation gemacht werden, potentiell nur mehr sehr wenig mit der Ausgangslage zu tun haben, beschränken wir uns für die Auswertung mittels RAVE auf jene Züge, welche aufgrund des Pfades in unserem Baum gewählt wurden plus die ersten 50 Züge der Simulation. Generell gewichten wir das Ergebnis der RAVE-Berechnungen höher wenn noch wenig Simulationen gemacht wurden und niedriger wenn bereits sehr viele Simulationen gemacht wurden (dann verlassen wir uns mehr auf das direkte Ergebnis der Simulationen).

Des weiteren werten wir Züge, bei denen gegnerische Steine geschlagen werden können, höher. Dieser Bonus fällt umso größer aus, je früher im Spiel wir uns befinden. Der Grund dafür ist, dass in der Anfangsphase die Ergebnisse der Simulationen noch sehr unzuverlässig sind und wir nicht auf das Schlagen von einem gegnerischen Stein aufgrund zufälliger Ergebnisse in den Simulationen verzichten wollen. In der Endphase hingegen verlassen wir uns mehr auf die Simulationen und vertrauen darauf, dass uns die MCTS den besten Zug liefert, auch wenn es auf den ersten Blick vielleicht nicht so aussieht.

Nach Ablauf der Zeit verwenden wir zur Selektion des Zuges eine ähnliche Formel wie beim Expandieren während der Suche. Der Exploration-Faktor wird hier dann aber abgezogen, statt dazu addiert. Dadurch verhindern wir, dass Züge mit einer guten Win-/Loss-Ratio aber sehr wenigen Simulationen gewählt werden. Solche Züge sind erfahrungsgemäß meist keine wirklich gute Wahl, da der zufällige Einfluss der Simulationen zu groß ist.