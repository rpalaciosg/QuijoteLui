    SELECT 
    CAST(t.TICKETID AS UNSIGNED INTEGER) AS id,
    (SELECT 
            id
        FROM
            v_ele_contribuyentes) AS id_contribuyente,
    CAST('NCV' AS CHAR (10)) AS codigo,
    CAST(CONCAT(FUN_ESTABLECIMIENTO(),
                FUN_PUNTO_EMISION(),
                LPAD(t.TICKETID, 9, '0'))
        AS CHAR (20)) AS numero,
    CAST('04' AS CHAR (10)) AS codigo_documento,
    FUN_ESTABLECIMIENTO() AS establecimiento,
    FUN_PUNTO_EMISION() AS punto_emision,
    CAST(LPAD(t.TICKETID, 9, '0') AS CHAR (10)) AS secuencial,
    CAST(r.DATENEW AS DATE) AS fecha,
    IF(c.POSTAL = 'Consumidor Final',
        '07',
        IF(c.POSTAL = 'RUC',
            '04',
            IF(c.POSTAL = 'Cédula', '05', '06'))) AS tipo_documento,
    c.TAXID AS documento,
    c.NAME AS razon_social,
    CAST('01' AS CHAR (10)) AS documento_modificado,
    FUN_MODIFICADO(t.id) AS modificado,
    FUN_FECHA_MODIFICADO(t.id) AS fecha_modificado,
    ROUND(SUM(CAST(IF(tx.RATE > 0, 0, tl.UNITS * tl.PRICE) AS DECIMAL (19 , 2 ))),
            2) AS total_sin_iva,
    ROUND(SUM(CAST(IF(tx.RATE > 0, tl.UNITS * tl.PRICE, 0) AS DECIMAL (19 , 2 ))),
            2) AS total_con_iva,
    ROUND(SUM(CAST(IF(tx.RATE > 0,
                    tl.UNITS * tl.PRICE * tx.RATE,
                    0)
                AS DECIMAL (19 , 2 ))),
            2) AS iva,
    ROUND(SUM(CAST(0 AS DECIMAL (19 , 2 )))) AS descuentos,
    ROUND(SUM(CAST(((tl.UNITS * tl.PRICE) + IF(tx.RATE > 0,
                    tl.UNITS * tl.PRICE * tx.RATE,
                    0))
                AS DECIMAL (19 , 2 ))),
            2) AS total,
    c.ADDRESS AS direccion,
    CAST(NULL AS CHAR (20)) AS guia_remision,
    (SELECT 
            con.direccion
        FROM
            v_ele_contribuyentes con) AS direccion_establecimiento
FROM
    openbravo.TICKETS t
        JOIN
    openbravo.RECEIPTS r ON t.id = r.id
        JOIN
    openbravo.CUSTOMERS c ON c.id = t.CUSTOMER
        JOIN
    openbravo.TICKETLINES tl ON t.ID = tl.TICKET
        JOIN
    openbravo.TAXES tx ON tx.category = tl.taxid
WHERE
    t.tickettype = 1
GROUP BY CAST(t.TICKETID AS UNSIGNED INTEGER) , (SELECT 
        id
    FROM
        v_ele_contribuyentes) , CAST('NCV' AS CHAR (10)) , CAST(CONCAT(FUN_ESTABLECIMIENTO(),
            FUN_PUNTO_EMISION(),
            LPAD(t.TICKETID, 9, '0'))
    AS CHAR (20)) , CAST('01' AS CHAR (10)) , FUN_ESTABLECIMIENTO() , FUN_PUNTO_EMISION() , CAST(LPAD(t.TICKETID, 9, '0') AS CHAR (10)) , CAST(r.DATENEW AS DATE) , IF(c.POSTAL = 'Consumidor Final',
    '07',
    IF(c.POSTAL = 'RUC',
        '04',
        IF(c.POSTAL = 'Cédula', '05', '06'))) , c.TAXID , c.NAME , '01' , FUN_MODIFICADO(t.id) , FUN_FECHA_MODIFICADO(t.id) , c.ADDRESS , CAST(NULL AS CHAR (20)) , (SELECT 
        con.direccion
    FROM
        v_ele_contribuyentes con);