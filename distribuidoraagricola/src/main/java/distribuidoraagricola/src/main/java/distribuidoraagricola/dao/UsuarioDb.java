package distribuidoraagricola.dao;

import distribuidoraagricola.modelo.Rol;
import distribuidoraagricola.modelo.Usuario;
import distribuidoraagricola.persistencia.PersistenciaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para Usuarios (cuentas de acceso al sistema) usando
 * JPA/EntityManager. idRol es columna simple, asi que nombreRol
 * (@Transient en la entidad) se llena aqui con una consulta a Roles
 * cuando hace falta mostrarlo.
 */
public class UsuarioDb {

    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.nombreUsuario = :nombreUsuario", Usuario.class);
            q.setParameter("nombreUsuario", nombreUsuario);
            Usuario u = q.getSingleResult();
            completarNombreRol(em, u);
            return u;
        } catch (NoResultException e) {
            return null;
        } catch (RuntimeException e) {
            System.out.println("Error al buscar usuario: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public Usuario buscarPorId(int idUsuario) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            Usuario u = em.find(Usuario.class, idUsuario);
            if (u != null) {
                completarNombreRol(em, u);
            }
            return u;
        } catch (RuntimeException e) {
            System.out.println("Error al buscar usuario: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public int insertar(Usuario u) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(u);
            em.getTransaction().commit();
            return u.getIdUsuario();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Error al crear usuario (verifica que el nombre no este repetido): " + e.getMessage());
            return -1;
        } finally {
            em.close();
        }
    }

    public List<Usuario> listarTodos() {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                    "SELECT u FROM Usuario u ORDER BY u.idUsuario", Usuario.class);
            List<Usuario> lista = q.getResultList();
            for (Usuario u : lista) {
                completarNombreRol(em, u);
            }
            return lista;
        } catch (RuntimeException e) {
            System.out.println("Error al listar usuarios: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public boolean existeNombreUsuario(String nombreUsuario) {
        return buscarPorNombreUsuario(nombreUsuario) != null;
    }

    public boolean cambiarPassword(int idUsuario, String nuevoSalt, String nuevoHash) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            Usuario u = em.find(Usuario.class, idUsuario);
            if (u == null) {
                em.getTransaction().rollback();
                return false;
            }
            u.setSalt(nuevoSalt);
            u.setHashContrasenia(nuevoHash);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Error al cambiar la contrasenia: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean cambiarEstado(int idUsuario, boolean estado) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            Usuario u = em.find(Usuario.class, idUsuario);
            if (u == null) {
                em.getTransaction().rollback();
                return false;
            }
            u.setEstado(estado);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Error al cambiar el estado del usuario: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    private void completarNombreRol(EntityManager em, Usuario u) {
        Rol r = em.find(Rol.class, u.getIdRol());
        if (r != null) {
            u.setNombreRol(r.getNombreRol());
        }
    }
}
