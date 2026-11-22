BEGIN;

-- 5 conducteurs de démonstration
INSERT INTO conducteurs (
    nom, prenom, email, telephone, numero_permis
)
SELECT
    'Demo',
    'Conducteur ' || n,
    'conducteur.demo' || n || '@example.com',
    '060000000' || n,
    'DEMO-PERMIS-' || n
FROM generate_series(1, 5) AS n
    ON CONFLICT DO NOTHING;


-- 5 passagers de démonstration
INSERT INTO passagers (nom, prenom, email, telephone)
SELECT
    'Demo',
    'Passager ' || n,
    'passager.demo' || n || '@example.com',
    '061000000' || n
FROM generate_series(1, 5) AS n
WHERE NOT EXISTS (
    SELECT 1
    FROM passagers p
    WHERE p.email = 'passager.demo' || n || '@example.com'
);


-- Un véhicule par conducteur de démonstration
INSERT INTO vehicules (
    marque, modele, immatriculation,
    couleur, capacite, conducteur_id
)
SELECT
    'Dacia',
    'Logan',
    'DEMO-VEHICULE-' || n,
    'Blanc',
    4,
    c.id
FROM generate_series(1, 5) AS n
         JOIN conducteurs c
              ON c.email = 'conducteur.demo' || n || '@example.com'
    ON CONFLICT DO NOTHING;


-- Un trajet futur par véhicule de démonstration
INSERT INTO trajets (
    conducteur_id, vehicule_id,
    ville_depart, ville_arrivee,
    date_depart, places_proposees, statut
)
SELECT
    v.conducteur_id,
    v.id,
    'Rabat',
    'Marrakech',
    CURRENT_DATE + INTERVAL '30 days 09 hours'
    + n * INTERVAL '1 day',
    3,
    'PUBLIE'
FROM generate_series(1, 5) AS n
    JOIN vehicules v
ON v.immatriculation = 'DEMO-VEHICULE-' || n
WHERE NOT EXISTS (
    SELECT 1
    FROM trajets t
    WHERE t.vehicule_id = v.id
    );


-- Point de départ : prix cumulatif 0 DH
INSERT INTO points_arret (
    trajet_id, ville, ordre,
    heure_passage, prix_depuis_depart
)
SELECT
    t.id,
    'Rabat',
    1,
    t.date_depart,
    0.00
FROM trajets t
         JOIN vehicules v ON v.id = t.vehicule_id
WHERE v.immatriculation IN (
                            'DEMO-VEHICULE-1',
                            'DEMO-VEHICULE-2',
                            'DEMO-VEHICULE-3',
                            'DEMO-VEHICULE-4',
                            'DEMO-VEHICULE-5'
    )
    ON CONFLICT DO NOTHING;


-- Point d'arrivée : prix cumulatif 120 DH
INSERT INTO points_arret (
    trajet_id, ville, ordre,
    heure_passage, prix_depuis_depart
)
SELECT
    t.id,
    'Marrakech',
    2,
    t.date_depart + INTERVAL '4 hours',
    120.00
FROM trajets t
    JOIN vehicules v ON v.id = t.vehicule_id
WHERE v.immatriculation IN (
    'DEMO-VEHICULE-1',
    'DEMO-VEHICULE-2',
    'DEMO-VEHICULE-3',
    'DEMO-VEHICULE-4',
    'DEMO-VEHICULE-5'
    )
ON CONFLICT DO NOTHING;


-- Une réservation d'une place par trajet : 120 DH
INSERT INTO reservations (
    passager_id, trajet_id,
    point_montee_id, point_descente_id,
    nombre_places, montant_total,
    date_reservation, statut
)
SELECT
    p.id,
    t.id,
    montee.id,
    descente.id,
    1,
    120.00,
    CURRENT_TIMESTAMP,
    'CONFIRMEE'
FROM generate_series(1, 5) AS n
         JOIN passagers p
              ON p.email = 'passager.demo' || n || '@example.com'
         JOIN vehicules v
              ON v.immatriculation = 'DEMO-VEHICULE-' || n
         JOIN trajets t
              ON t.vehicule_id = v.id
         JOIN points_arret montee
              ON montee.trajet_id = t.id AND montee.ordre = 1
         JOIN points_arret descente
              ON descente.trajet_id = t.id AND descente.ordre = 2
WHERE NOT EXISTS (
    SELECT 1
    FROM reservations r
    WHERE r.passager_id = p.id
      AND r.trajet_id = t.id
);

COMMIT;