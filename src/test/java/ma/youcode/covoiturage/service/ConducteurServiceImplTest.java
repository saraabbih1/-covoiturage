package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.mapper.ConducteurMapper;
import ma.youcode.covoiturage.repository.ConducteurRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ma.youcode.covoiturage.dto.ConducteurRequest;
import ma.youcode.covoiturage.dto.ConducteurResponse;
import ma.youcode.covoiturage.entity.Conducteur;
import org.junit.jupiter.api.Test;
import ma.youcode.covoiturage.exception.ConflictException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class ConducteurServiceImplTest {

    @Mock
    private ConducteurRepository repository;

    @Mock
    private ConducteurMapper mapper;

    @InjectMocks
    private ConducteurServiceImpl service;
    @Test
    void creer_doitRetournerConducteur_quandDonneesValides() {

        // GIVEN : préparer les données et les réponses des mocks
        ConducteurRequest request = new ConducteurRequest(
                "Alami",
                "Hamid",
                "hamid@example.com",
                "0600000000",
                "ABC123"
        );

        Conducteur conducteur = new Conducteur();

        Conducteur saved = new Conducteur();
        saved.setId(1L);

        ConducteurResponse expected = new ConducteurResponse(
                1L,
                "Alami",
                "Hamid",
                "hamid@example.com",
                "0600000000",
                "ABC123"
        );

        when(repository.existsByEmail(request.email())).thenReturn(false);
        when(repository.existsByNumeroPermis(request.numeroPermis()))
                .thenReturn(false);

        when(mapper.toEntity(request)).thenReturn(conducteur);
        when(repository.save(conducteur)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(expected);


        ConducteurResponse result = service.creer(request);


        assertThat(result).isEqualTo(expected);

        verify(repository).save(conducteur);
    }
    @Test
    void creer_doitRefuser_quandEmailDejaUtilise() {


        ConducteurRequest request = new ConducteurRequest(
                "Alami",
                "Hamid",
                "hamid@example.com",
                "0600000000",
                "ABC123"
        );

        when(repository.existsByEmail(request.email()))
                .thenReturn(true);


        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email deja utilise");

        verify(repository, never()).save(any(Conducteur.class));
    }
    @Test
    void creer_doitRefuser_quandNumeroPermisDejaUtilise() {


        ConducteurRequest request = new ConducteurRequest(
                "Alami",
                "Hamid",
                "hamid@example.com",
                "0600000000",
                "ABC123"
        );

        when(repository.existsByEmail(request.email()))
                .thenReturn(false);

        when(repository.existsByNumeroPermis(request.numeroPermis()))
                .thenReturn(true);


        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Numero de permis deja utilise");

        verify(repository, never()).save(any(Conducteur.class));
    }
}