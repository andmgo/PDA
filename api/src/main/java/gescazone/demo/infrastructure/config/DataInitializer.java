package gescazone.demo.infrastructure.config;

import gescazone.demo.domain.model.RolModel;
import gescazone.demo.domain.model.TipoDocumentoModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos semilla solo para desarrollo. Activo únicamente bajo el perfil "dev"
 * (por defecto en local); en Azure/producción se activa el perfil "prod"
 * (SPRING_PROFILES_ACTIVE=prod) y esta clase no se ejecuta.
 */
@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RolModel rol(String nombre) {
        RolModel r = new RolModel();
        // Normalizado a mayúsculas para coincidir con el catálogo "roles" sembrado
        // por Flyway (ADMINISTRADOR/PROPIETARIO/FUNCIONARIO) y con RolNombre — evita
        // crear filas de catálogo duplicadas por diferencias de mayúsculas/minúsculas.
        r.setNombreRol(nombre.toUpperCase());
        return r;
    }

    private TipoDocumentoModel tipoDoc(String nombre) {
        TipoDocumentoModel t = new TipoDocumentoModel();
        t.setNombreTipoDocumento(nombre);
        return t;
    }

    private UsuarioModel usuario(String doc, String nombre, String apellido,
                                 String correo, String pass,
                                 String rolNombre, String tipoDocNombre) {
        UsuarioModel u = new UsuarioModel();
        u.setNumeroDocumento(doc);
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setCorreo(correo);
        u.setContrasena(passwordEncoder.encode(pass));
        u.setRol(rol(rolNombre));
        u.setTipoDocumento(tipoDoc(tipoDocNombre));
        return u;
    }

    private void guardar(UsuarioModel u) {
        if (!usuarioRepository.existsByNumeroDocumento(u.getNumeroDocumento())) {
            usuarioRepository.save(u);
            log.info("Usuario semilla creado: {} / {} {}", u.getNumeroDocumento(), u.getNombre(), u.getApellido());
        } else {
            log.debug("Usuario semilla ya existe: {}", u.getNumeroDocumento());
        }
    }

    @Override
    public void run(String... args) throws Exception {

        // ── Usuarios originales ───────────────────────────────────────────────

        guardar(usuario("123456780", "Admin",  "Sistema",     "admin@gescazone.com",       "admin123", "Administrador", "CC"));
        guardar(usuario("123456781", "Carlos", "Propietario", "propietario@gescazone.com", "prop123",  "Propietario",   "CC"));
        guardar(usuario("123456782", "Laura",  "Funcionario", "funcionario@gescazone.com", "func123",  "Funcionario",   "CC"));

        // ── 150 usuarios adicionales ──────────────────────────────────────────

        List<UsuarioModel> adicionales = new ArrayList<>();

        // ── ADMINISTRADORES (20) ──────────────────────────────────────────────
        adicionales.add(usuario("100000001", "Sofía",     "Ramírez",   "sofia.ramirez@gescazone.com",    "adm001", "Administrador", "CC"));
        adicionales.add(usuario("100000002", "Miguel",    "Torres",    "miguel.torres@gescazone.com",    "adm002", "Administrador", "CC"));
        adicionales.add(usuario("100000003", "Valentina", "Herrera",   "val.herrera@gescazone.com",      "adm003", "Administrador", "CC"));
        adicionales.add(usuario("100000004", "Andrés",    "Ospina",    "andres.ospina@gescazone.com",    "adm004", "Administrador", "CC"));
        adicionales.add(usuario("100000005", "Camila",    "Vargas",    "camila.vargas@gescazone.com",    "adm005", "Administrador", "CE"));
        adicionales.add(usuario("100000006", "Ricardo",   "Molina",    "ricardo.molina@gescazone.com",   "adm006", "Administrador", "CC"));
        adicionales.add(usuario("100000007", "Isabella",  "Castro",    "isabella.castro@gescazone.com",  "adm007", "Administrador", "CC"));
        adicionales.add(usuario("100000008", "Felipe",    "Moreno",    "felipe.moreno@gescazone.com",    "adm008", "Administrador", "CE"));
        adicionales.add(usuario("100000009", "Natalia",   "Jiménez",   "natalia.jimenez@gescazone.com",  "adm009", "Administrador", "CC"));
        adicionales.add(usuario("100000010", "Sebastián", "Ruiz",      "sebastian.ruiz@gescazone.com",   "adm010", "Administrador", "CC"));
        adicionales.add(usuario("100000011", "Daniela",   "Peña",      "daniela.pena@gescazone.com",     "adm011", "Administrador", "CC"));
        adicionales.add(usuario("100000012", "Julián",    "Salazar",   "julian.salazar@gescazone.com",   "adm012", "Administrador", "CE"));
        adicionales.add(usuario("100000013", "Mariana",   "Gómez",     "mariana.gomez@gescazone.com",    "adm013", "Administrador", "CC"));
        adicionales.add(usuario("100000014", "Esteban",   "Mendoza",   "esteban.mendoza@gescazone.com",  "adm014", "Administrador", "CC"));
        adicionales.add(usuario("100000015", "Gabriela",  "Ríos",      "gabriela.rios@gescazone.com",    "adm015", "Administrador", "CC"));
        adicionales.add(usuario("100000016", "Alejandro", "Suárez",    "alejandro.suarez@gescazone.com", "adm016", "Administrador", "CC"));
        adicionales.add(usuario("100000017", "Paola",     "Aguilar",   "paola.aguilar@gescazone.com",    "adm017", "Administrador", "CE"));
        adicionales.add(usuario("100000018", "Diego",     "Navarro",   "diego.navarro@gescazone.com",    "adm018", "Administrador", "CC"));
        adicionales.add(usuario("100000019", "Manuela",   "Sandoval",  "manuela.sandoval@gescazone.com", "adm019", "Administrador", "CC"));
        adicionales.add(usuario("100000020", "Tomás",     "Guerrero",  "tomas.guerrero@gescazone.com",   "adm020", "Administrador", "CC"));

        // ── FUNCIONARIOS (30) ─────────────────────────────────────────────────
        adicionales.add(usuario("200000001", "Andrea",    "Lozano",    "andrea.lozano@gescazone.com",    "fun001", "Funcionario", "CC"));
        adicionales.add(usuario("200000002", "Hernán",    "Cardona",   "hernan.cardona@gescazone.com",   "fun002", "Funcionario", "CC"));
        adicionales.add(usuario("200000003", "Lucía",     "Patiño",    "lucia.patino@gescazone.com",     "fun003", "Funcionario", "CC"));
        adicionales.add(usuario("200000004", "Mauricio",  "Quintero",  "mauricio.quintero@gescazone.com","fun004", "Funcionario", "CE"));
        adicionales.add(usuario("200000005", "Ximena",    "Castaño",   "ximena.castano@gescazone.com",   "fun005", "Funcionario", "CC"));
        adicionales.add(usuario("200000006", "Rodrigo",   "Díaz",      "rodrigo.diaz@gescazone.com",     "fun006", "Funcionario", "CE"));
        adicionales.add(usuario("200000007", "Alejandra", "Mejía",     "alejandra.mejia@gescazone.com",  "fun007", "Funcionario", "CC"));
        adicionales.add(usuario("200000008", "Jorge",     "Pineda",    "jorge.pineda@gescazone.com",     "fun008", "Funcionario", "CC"));
        adicionales.add(usuario("200000009", "Carolina",  "Acosta",    "carolina.acosta@gescazone.com",  "fun009", "Funcionario", "CC"));
        adicionales.add(usuario("200000010", "Ernesto",   "Valencia",  "ernesto.valencia@gescazone.com", "fun010", "Funcionario", "CC"));
        adicionales.add(usuario("200000011", "Tatiana",   "Escobar",   "tatiana.escobar@gescazone.com",  "fun011", "Funcionario", "CC"));
        adicionales.add(usuario("200000012", "Gustavo",   "Restrepo",  "gustavo.restrepo@gescazone.com", "fun012", "Funcionario", "CE"));
        adicionales.add(usuario("200000013", "Mónica",    "Arbeláez",  "monica.arbelaez@gescazone.com",  "fun013", "Funcionario", "CC"));
        adicionales.add(usuario("200000014", "Iván",      "Bermúdez",  "ivan.bermudez@gescazone.com",    "fun014", "Funcionario", "CC"));
        adicionales.add(usuario("200000015", "Patricia",  "Montoya",   "patricia.montoya@gescazone.com", "fun015", "Funcionario", "CC"));
        adicionales.add(usuario("200000016", "Álvaro",    "Hurtado",   "alvaro.hurtado@gescazone.com",   "fun016", "Funcionario", "CE"));
        adicionales.add(usuario("200000017", "Sandra",    "Zapata",    "sandra.zapata@gescazone.com",    "fun017", "Funcionario", "CC"));
        adicionales.add(usuario("200000018", "Cristian",  "Muñoz",     "cristian.munoz@gescazone.com",   "fun018", "Funcionario", "CC"));
        adicionales.add(usuario("200000019", "Liliana",   "Franco",    "liliana.franco@gescazone.com",   "fun019", "Funcionario", "CC"));
        adicionales.add(usuario("200000020", "Nelson",    "Ibáñez",    "nelson.ibanez@gescazone.com",    "fun020", "Funcionario", "CC"));
        adicionales.add(usuario("200000021", "Esperanza", "Cárdenas",  "esperanza.cardenas@gescazone.com","fun021","Funcionario", "CC"));
        adicionales.add(usuario("200000022", "Fabio",     "Londoño",   "fabio.londono@gescazone.com",    "fun022", "Funcionario", "CE"));
        adicionales.add(usuario("200000023", "Claudia",   "Arango",    "claudia.arango@gescazone.com",   "fun023", "Funcionario", "CC"));
        adicionales.add(usuario("200000024", "Samuel",    "Bernal",    "samuel.bernal@gescazone.com",    "fun024", "Funcionario", "CC"));
        adicionales.add(usuario("200000025", "Adriana",   "Cano",      "adriana.cano@gescazone.com",     "fun025", "Funcionario", "CC"));
        adicionales.add(usuario("200000026", "Óscar",     "Duque",     "oscar.duque@gescazone.com",      "fun026", "Funcionario", "CE"));
        adicionales.add(usuario("200000027", "Viviana",   "Arias",     "viviana.arias@gescazone.com",    "fun027", "Funcionario", "CC"));
        adicionales.add(usuario("200000028", "Jhon",      "Rojas",     "jhon.rojas@gescazone.com",       "fun028", "Funcionario", "CC"));
        adicionales.add(usuario("200000029", "Nathalia",  "Sierra",    "nathalia.sierra@gescazone.com",  "fun029", "Funcionario", "CC"));
        adicionales.add(usuario("200000030", "Wilmar",    "Pulido",    "wilmar.pulido@gescazone.com",    "fun030", "Funcionario", "CC"));

        // ── PROPIETARIOS (100) ────────────────────────────────────────────────
        adicionales.add(usuario("300000001", "Elena",     "Vásquez",   "elena.vasquez@gmail.com",        "pro001", "Propietario", "CC"));
        adicionales.add(usuario("300000002", "Roberto",   "Ochoa",     "roberto.ochoa@gmail.com",        "pro002", "Propietario", "CC"));
        adicionales.add(usuario("300000003", "Pilar",     "Useche",    "pilar.useche@gmail.com",         "pro003", "Propietario", "CE"));
        adicionales.add(usuario("300000004", "Germán",    "Forero",    "german.forero@gmail.com",        "pro004", "Propietario", "CC"));
        adicionales.add(usuario("300000005", "Beatriz",   "Páez",      "beatriz.paez@gmail.com",         "pro005", "Propietario", "CC"));
        adicionales.add(usuario("300000006", "Armando",   "Vélez",     "armando.velez@gmail.com",        "pro006", "Propietario", "CE"));
        adicionales.add(usuario("300000007", "Gloria",    "Nieto",     "gloria.nieto@gmail.com",         "pro007", "Propietario", "CC"));
        adicionales.add(usuario("300000008", "Jairo",     "Toro",      "jairo.toro@gmail.com",           "pro008", "Propietario", "CC"));
        adicionales.add(usuario("300000009", "Martha",    "Orjuela",   "martha.orjuela@gmail.com",       "pro009", "Propietario", "CC"));
        adicionales.add(usuario("300000010", "Héctor",    "Cifuentes", "hector.cifuentes@gmail.com",     "pro010", "Propietario", "TI"));
        adicionales.add(usuario("300000011", "Amparo",    "Galvis",    "amparo.galvis@gmail.com",        "pro011", "Propietario", "CC"));
        adicionales.add(usuario("300000012", "Hernando",  "Pedraza",   "hernando.pedraza@gmail.com",     "pro012", "Propietario", "CE"));
        adicionales.add(usuario("300000013", "Constanza", "Reyes",     "constanza.reyes@gmail.com",      "pro013", "Propietario", "CC"));
        adicionales.add(usuario("300000014", "Gilberto",  "Serrano",   "gilberto.serrano@gmail.com",     "pro014", "Propietario", "CC"));
        adicionales.add(usuario("300000015", "Rosario",   "Rincón",    "rosario.rincon@gmail.com",       "pro015", "Propietario", "CC"));
        adicionales.add(usuario("300000016", "Porfirio",  "Blanco",    "porfirio.blanco@gmail.com",      "pro016", "Propietario", "CE"));
        adicionales.add(usuario("300000017", "Amparo",    "Luna",      "amparo.luna@gmail.com",          "pro017", "Propietario", "CC"));
        adicionales.add(usuario("300000018", "Rodrigo",   "Estrada",   "rodrigo.estrada@gmail.com",      "pro018", "Propietario", "CC"));
        adicionales.add(usuario("300000019", "Cecilia",   "Alvarado",  "cecilia.alvarado@gmail.com",     "pro019", "Propietario", "TI"));
        adicionales.add(usuario("300000020", "Bernardo",  "Cortés",    "bernardo.cortes@gmail.com",      "pro020", "Propietario", "CC"));
        adicionales.add(usuario("300000021", "Yolanda",   "Parra",     "yolanda.parra@gmail.com",        "pro021", "Propietario", "CC"));
        adicionales.add(usuario("300000022", "Aurelio",   "Mora",      "aurelio.mora@gmail.com",         "pro022", "Propietario", "CE"));
        adicionales.add(usuario("300000023", "Inés",      "Olmos",     "ines.olmos@gmail.com",           "pro023", "Propietario", "CC"));
        adicionales.add(usuario("300000024", "Ramiro",    "Gil",       "ramiro.gil@gmail.com",           "pro024", "Propietario", "CC"));
        adicionales.add(usuario("300000025", "Lucelly",   "Zapata",    "lucelly.zapata@gmail.com",       "pro025", "Propietario", "CC"));
        adicionales.add(usuario("300000026", "Gonzalo",   "Urrego",    "gonzalo.urrego@gmail.com",       "pro026", "Propietario", "CE"));
        adicionales.add(usuario("300000027", "Flor",      "Henao",     "flor.henao@gmail.com",           "pro027", "Propietario", "CC"));
        adicionales.add(usuario("300000028", "Néstor",    "Bedoya",    "nestor.bedoya@gmail.com",        "pro028", "Propietario", "CC"));
        adicionales.add(usuario("300000029", "Alba",      "Sepúlveda", "alba.sepulveda@gmail.com",       "pro029", "Propietario", "TI"));
        adicionales.add(usuario("300000030", "Efraín",    "Ossa",      "efrain.ossa@gmail.com",          "pro030", "Propietario", "CC"));
        adicionales.add(usuario("300000031", "Nubia",     "Palomino",  "nubia.palomino@gmail.com",       "pro031", "Propietario", "CC"));
        adicionales.add(usuario("300000032", "Álvaro",    "Rengifo",   "alvaro.rengifo@gmail.com",       "pro032", "Propietario", "CE"));
        adicionales.add(usuario("300000033", "Dora",      "Machado",   "dora.machado@gmail.com",         "pro033", "Propietario", "CC"));
        adicionales.add(usuario("300000034", "Rigoberto", "Cuellar",   "rigoberto.cuellar@gmail.com",    "pro034", "Propietario", "CC"));
        adicionales.add(usuario("300000035", "Edith",     "Trujillo",  "edith.trujillo@gmail.com",       "pro035", "Propietario", "CC"));
        adicionales.add(usuario("300000036", "Libardo",   "Gutiérrez", "libardo.gutierrez@gmail.com",    "pro036", "Propietario", "CE"));
        adicionales.add(usuario("300000037", "Consuelo",  "Buitrago",  "consuelo.buitrago@gmail.com",    "pro037", "Propietario", "CC"));
        adicionales.add(usuario("300000038", "Eucario",   "Meza",      "eucario.meza@gmail.com",         "pro038", "Propietario", "CC"));
        adicionales.add(usuario("300000039", "Miriam",    "Cabrera",   "miriam.cabrera@gmail.com",       "pro039", "Propietario", "TI"));
        adicionales.add(usuario("300000040", "Silvio",    "Pastrana",  "silvio.pastrana@gmail.com",      "pro040", "Propietario", "CC"));
        adicionales.add(usuario("300000041", "Esperanza", "Ramos",     "esperanza.ramos@gmail.com",      "pro041", "Propietario", "CC"));
        adicionales.add(usuario("300000042", "Alirio",    "Muñetón",   "alirio.muneton@gmail.com",       "pro042", "Propietario", "CE"));
        adicionales.add(usuario("300000043", "Fanny",     "Tobón",     "fanny.tobon@gmail.com",          "pro043", "Propietario", "CC"));
        adicionales.add(usuario("300000044", "Jaime",     "Zea",       "jaime.zea@gmail.com",            "pro044", "Propietario", "CC"));
        adicionales.add(usuario("300000045", "Olga",      "Pinzón",    "olga.pinzon@gmail.com",          "pro045", "Propietario", "CC"));
        adicionales.add(usuario("300000046", "Cielo",     "Arboleda",  "cielo.arboleda@gmail.com",       "pro046", "Propietario", "CE"));
        adicionales.add(usuario("300000047", "Luis",      "Cárdenas",  "luis.cardenas@gmail.com",        "pro047", "Propietario", "CC"));
        adicionales.add(usuario("300000048", "Teresa",    "Largo",     "teresa.largo@gmail.com",         "pro048", "Propietario", "TI"));
        adicionales.add(usuario("300000049", "Fidel",     "Bravo",     "fidel.bravo@gmail.com",          "pro049", "Propietario", "CC"));
        adicionales.add(usuario("300000050", "Amparo",    "Quintana",  "amparo.quintana@gmail.com",      "pro050", "Propietario", "CC"));
        adicionales.add(usuario("300000051", "Jairo",     "Sánchez",   "jairo.sanchez@gmail.com",        "pro051", "Propietario", "CC"));
        adicionales.add(usuario("300000052", "Luz",       "Cardozo",   "luz.cardozo@gmail.com",          "pro052", "Propietario", "CE"));
        adicionales.add(usuario("300000053", "Duván",     "Herrera",   "duvan.herrera@gmail.com",        "pro053", "Propietario", "CC"));
        adicionales.add(usuario("300000054", "Rocío",     "Salcedo",   "rocio.salcedo@gmail.com",        "pro054", "Propietario", "CC"));
        adicionales.add(usuario("300000055", "Guillermo", "Agudelo",   "guillermo.agudelo@gmail.com",    "pro055", "Propietario", "CC"));
        adicionales.add(usuario("300000056", "Marta",     "Villanueva","marta.villanueva@gmail.com",     "pro056", "Propietario", "CE"));
        adicionales.add(usuario("300000057", "Clímaco",   "Palacio",   "climaco.palacio@gmail.com",      "pro057", "Propietario", "CC"));
        adicionales.add(usuario("300000058", "Liliana",   "Holguín",   "liliana.holguin@gmail.com",      "pro058", "Propietario", "TI"));
        adicionales.add(usuario("300000059", "Ferney",    "Giraldo",   "ferney.giraldo@gmail.com",       "pro059", "Propietario", "CC"));
        adicionales.add(usuario("300000060", "Yenny",     "Osorio",    "yenny.osorio@gmail.com",         "pro060", "Propietario", "CC"));
        adicionales.add(usuario("300000061", "Aníbal",    "Loaiza",    "anibal.loaiza@gmail.com",        "pro061", "Propietario", "CC"));
        adicionales.add(usuario("300000062", "Blanca",    "Marulanda", "blanca.marulanda@gmail.com",     "pro062", "Propietario", "CE"));
        adicionales.add(usuario("300000063", "Camilo",    "Tobón",     "camilo.tobon@gmail.com",         "pro063", "Propietario", "CC"));
        adicionales.add(usuario("300000064", "Diana",     "Echeverri", "diana.echeverri@gmail.com",      "pro064", "Propietario", "CC"));
        adicionales.add(usuario("300000065", "Ernesto",   "Ocampo",    "ernesto.ocampo@gmail.com",       "pro065", "Propietario", "CC"));
        adicionales.add(usuario("300000066", "Fabiana",   "Ríos",      "fabiana.rios@gmail.com",         "pro066", "Propietario", "CE"));
        adicionales.add(usuario("300000067", "Gustavo",   "Naranjo",   "gustavo.naranjo@gmail.com",      "pro067", "Propietario", "CC"));
        adicionales.add(usuario("300000068", "Hilda",     "Posada",    "hilda.posada@gmail.com",         "pro068", "Propietario", "TI"));
        adicionales.add(usuario("300000069", "Ignacio",   "Tamayo",    "ignacio.tamayo@gmail.com",       "pro069", "Propietario", "CC"));
        adicionales.add(usuario("300000070", "Jackeline", "Duque",     "jackeline.duque@gmail.com",      "pro070", "Propietario", "CC"));
        adicionales.add(usuario("300000071", "Kevin",     "Aristizábal","kevin.aristizabal@gmail.com",   "pro071", "Propietario", "CC"));
        adicionales.add(usuario("300000072", "Lorena",    "Betancur",  "lorena.betancur@gmail.com",      "pro072", "Propietario", "CE"));
        adicionales.add(usuario("300000073", "Marcos",    "Yepes",     "marcos.yepes@gmail.com",         "pro073", "Propietario", "CC"));
        adicionales.add(usuario("300000074", "Nancy",     "Londoño",   "nancy.londono@gmail.com",        "pro074", "Propietario", "CC"));
        adicionales.add(usuario("300000075", "Orlando",   "Cano",      "orlando.cano@gmail.com",         "pro075", "Propietario", "CC"));
        adicionales.add(usuario("300000076", "Paulina",   "Muñoz",     "paulina.munoz@gmail.com",        "pro076", "Propietario", "CE"));
        adicionales.add(usuario("300000077", "Quintín",   "Vanegas",   "quintin.vanegas@gmail.com",      "pro077", "Propietario", "CC"));
        adicionales.add(usuario("300000078", "Rebeca",    "Zuluaga",   "rebeca.zuluaga@gmail.com",       "pro078", "Propietario", "TI"));
        adicionales.add(usuario("300000079", "Sergio",    "Alzate",    "sergio.alzate@gmail.com",        "pro079", "Propietario", "CC"));
        adicionales.add(usuario("300000080", "Tatiana",   "Monsalve",  "tatiana.monsalve@gmail.com",     "pro080", "Propietario", "CC"));
        adicionales.add(usuario("300000081", "Uberney",   "Palacios",  "uberney.palacios@gmail.com",     "pro081", "Propietario", "CC"));
        adicionales.add(usuario("300000082", "Valentina", "Caballero", "valentina.caballero@gmail.com",  "pro082", "Propietario", "CE"));
        adicionales.add(usuario("300000083", "Wilson",    "Atehortúa", "wilson.atehortua@gmail.com",     "pro083", "Propietario", "CC"));
        adicionales.add(usuario("300000084", "Xiomara",   "Mosquera",  "xiomara.mosquera@gmail.com",     "pro084", "Propietario", "CC"));
        adicionales.add(usuario("300000085", "Yamile",    "Garcés",    "yamile.garces@gmail.com",        "pro085", "Propietario", "CC"));
        adicionales.add(usuario("300000086", "Zaira",     "Guzmán",    "zaira.guzman@gmail.com",         "pro086", "Propietario", "CE"));
        adicionales.add(usuario("300000087", "Alfredo",   "Leal",      "alfredo.leal@gmail.com",         "pro087", "Propietario", "CC"));
        adicionales.add(usuario("300000088", "Bertha",    "Cuervo",    "bertha.cuervo@gmail.com",        "pro088", "Propietario", "TI"));
        adicionales.add(usuario("300000089", "César",     "Arbeláez",  "cesar.arbelaez@gmail.com",       "pro089", "Propietario", "CC"));
        adicionales.add(usuario("300000090", "Doris",     "Cadena",    "doris.cadena@gmail.com",         "pro090", "Propietario", "CC"));
        adicionales.add(usuario("300000091", "Elkin",     "Varela",    "elkin.varela@gmail.com",         "pro091", "Propietario", "CC"));
        adicionales.add(usuario("300000092", "Fabiola",   "Penagos",   "fabiola.penagos@gmail.com",      "pro092", "Propietario", "CE"));
        adicionales.add(usuario("300000093", "Gregorio",  "Salinas",   "gregorio.salinas@gmail.com",     "pro093", "Propietario", "CC"));
        adicionales.add(usuario("300000094", "Hortensia", "Castillo",  "hortensia.castillo@gmail.com",   "pro094", "Propietario", "CC"));
        adicionales.add(usuario("300000095", "Isaías",    "Bermejo",   "isaias.bermejo@gmail.com",       "pro095", "Propietario", "CC"));
        adicionales.add(usuario("300000096", "Josefina",  "Quiroga",   "josefina.quiroga@gmail.com",     "pro096", "Propietario", "CE"));
        adicionales.add(usuario("300000097", "Kelvin",    "Correa",    "kelvin.correa@gmail.com",        "pro097", "Propietario", "TI"));
        adicionales.add(usuario("300000098", "Lina",      "Ávila",     "lina.avila@gmail.com",           "pro098", "Propietario", "CC"));
        adicionales.add(usuario("300000099", "Marisol",   "Tobón",     "marisol.tobon@gmail.com",        "pro099", "Propietario", "CC"));
        adicionales.add(usuario("300000100", "Norberto",  "Ospina",    "norberto.ospina@gmail.com",      "pro100", "Propietario", "CC"));

        adicionales.forEach(this::guardar);
    }
}