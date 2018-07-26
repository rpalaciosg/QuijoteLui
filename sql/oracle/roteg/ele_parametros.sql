CREATE TABLE ELE_PARAMETROS (
    "ID"            NUMBER,
    "NOMBRE"        VARCHAR2(100 BYTE),
    "VALOR"         VARCHAR2(200 BYTE),
    "OBSERVACION"   VARCHAR2(500 BYTE),
    "TIPO"   VARCHAR2(20 BYTE),
    "ESTADO"        VARCHAR2(20 BYTE)
)
    TABLESPACE "USERS";

ALTER TABLE "ELE_PARAMETROS"
    ADD CONSTRAINT "PK_ELE_PARAMETROS" PRIMARY KEY ( "ID" )
        USING INDEX TABLESPACE "USERS"
    ENABLE;

ALTER TABLE "ELE_PARAMETROS" MODIFY (
    "ID"
        NOT NULL ENABLE
);