package gescazone.demo.infrastructure.config;

import gescazone.demo.domain.model.RolModel;
import gescazone.demo.domain.model.TipoDocumentoModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
public void run(String... args) throws Exception {

    // ── ADMINISTRADOR ──────────────────────────────────────────
    if (!usuarioRepository.existsByNumeroDocumento("123456780")) {
        RolModel rol = new RolModel();
        rol.setNombreRol("ADMINISTRADOR");

        TipoDocumentoModel tipoDoc = new TipoDocumentoModel();
        tipoDoc.setNombreTipoDocumento("Cédula");

        UsuarioModel admin = new UsuarioModel();
        admin.setNumeroDocumento("123456780");
        admin.setNombre("Admin");
        admin.setApellido("Sistema");
        admin.setCorreo("admin@gescazone.com");
        admin.setContrasena(passwordEncoder.encode("admin123"));
        admin.setRol(rol);
        admin.setTipoDocumento(tipoDoc);

        usuarioRepository.save(admin);
        System.out.println("✅ Administrador creado: 123456780 / admin123");
    } else {
        System.out.println("ℹ️ Administrador ya existe");
    }

    // ── PROPIETARIO ────────────────────────────────────────────
    if (!usuarioRepository.existsByNumeroDocumento("123456781")) {
        RolModel rol = new RolModel();
        rol.setNombreRol("PROPIETARIO");

        TipoDocumentoModel tipoDoc = new TipoDocumentoModel();
        tipoDoc.setNombreTipoDocumento("Cédula");

        UsuarioModel propietario = new UsuarioModel();
        propietario.setNumeroDocumento("123456781");
        propietario.setNombre("Carlos");
        propietario.setApellido("Propietario");
        propietario.setCorreo("propietario@gescazone.com");
        propietario.setContrasena(passwordEncoder.encode("prop123"));
        propietario.setRol(rol);
        propietario.setTipoDocumento(tipoDoc);

        usuarioRepository.save(propietario);
        System.out.println("✅ Propietario creado: 123456781 / prop123");
    } else {
        System.out.println("ℹ️ Propietario ya existe");
    }

    // ── FUNCIONARIO ────────────────────────────────────────────
    if (!usuarioRepository.existsByNumeroDocumento("123456782")) {
        RolModel rol = new RolModel();
        rol.setNombreRol("FUNCIONARIO");

        TipoDocumentoModel tipoDoc = new TipoDocumentoModel();
        tipoDoc.setNombreTipoDocumento("Cédula");

        UsuarioModel funcionario = new UsuarioModel();
        funcionario.setNumeroDocumento("123456782");
        funcionario.setNombre("Laura");
        funcionario.setApellido("Funcionario");
        funcionario.setCorreo("funcionario@gescazone.com");
        funcionario.setContrasena(passwordEncoder.encode("func123"));
        funcionario.setRol(rol);
        funcionario.setTipoDocumento(tipoDoc);

        usuarioRepository.save(funcionario);
        System.out.println("✅ Funcionario creado: 123456782 / func123");
    } else {
        System.out.println("ℹ️ Funcionario ya existe");
    }
}
}