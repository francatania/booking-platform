package com.booking.companyservice.service;

import com.booking.companyservice.config.UserPrincipal;
import com.booking.companyservice.entity.Company;
import com.booking.companyservice.entity.CompanyServiceEntity;
import com.booking.companyservice.exception.CompanyNotFoundException;
import com.booking.companyservice.exception.CompanyServiceNotFoundException;
import com.booking.companyservice.model.dto.CompanyServiceResponse;
import com.booking.companyservice.model.dto.CreateCompanyServiceRequest;
import com.booking.companyservice.model.dto.UpdateCompanyServiceRequest;
import com.booking.companyservice.repository.CompanyRepository;
import com.booking.companyservice.repository.CompanyServiceRepository;
import com.booking.companyservice.service.impl.CompanyServiceService;
import com.booking.companyservice.service.interfaces.ICompanyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceServiceTest {

    private static final Long COMPANY_ID = 1L;

    @Mock
    private CompanyServiceRepository companyServiceRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ICompanyService companyService;

    @InjectMocks
    private CompanyServiceService companyServiceService;

    private Method getCheckOwnership() throws NoSuchMethodException {
        Method method = CompanyServiceService.class.getDeclaredMethod("checkOwnership", UserPrincipal.class, Long.class);
        method.setAccessible(true);
        return method;
    }

    @Test
    void getServicesByCompany_whenCompanyNotFound_throwsException() {
        when(companyService.getCompanyEntity(COMPANY_ID))
            .thenThrow(new CompanyNotFoundException(COMPANY_ID));

        assertThrows(CompanyNotFoundException.class, () -> companyServiceService.getServicesByCompany(COMPANY_ID));
    }

    @Test
    void getServicesByCompany_whenCompanyExists_returnsList() {
        Company company = Company.builder().id(COMPANY_ID).build();
        CompanyServiceEntity service1 = CompanyServiceEntity.builder().id(1L).name("Corte").company(company).build();
        CompanyServiceEntity service2 = CompanyServiceEntity.builder().id(2L).name("Tinte").company(company).build();

        when(companyServiceRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(service1, service2));

        List<CompanyServiceResponse> result = companyServiceService.getServicesByCompany(COMPANY_ID);

        assertEquals(2, result.size());
    }

    @Test
    void createService_whenUserRole_throwsAccessDenied() {
        UserPrincipal principal = new UserPrincipal("user@test.com", "USER", COMPANY_ID);
        CreateCompanyServiceRequest dto = new CreateCompanyServiceRequest();

        assertThrows(AccessDeniedException.class, () -> companyServiceService.createService(dto, COMPANY_ID, principal));
    }

    @Test
    void createService_whenAdminDoesNotOwnCompany_throwsAccessDenied() {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", 2L);
        CreateCompanyServiceRequest dto = new CreateCompanyServiceRequest();

        assertThrows(AccessDeniedException.class, () -> companyServiceService.createService(dto, COMPANY_ID, principal));
    }

    @Test
    void createService_whenCompanyNotFound_throwsException() {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", COMPANY_ID);
        CreateCompanyServiceRequest dto = new CreateCompanyServiceRequest();

        when(companyService.getCompanyEntity(COMPANY_ID)).thenThrow(new CompanyNotFoundException(COMPANY_ID));

        assertThrows(CompanyNotFoundException.class, () -> companyServiceService.createService(dto, COMPANY_ID, principal));
    }

    @Test
    void createService_whenValidRequest_returnsResponse() {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", COMPANY_ID);
        CreateCompanyServiceRequest dto = new CreateCompanyServiceRequest();
        dto.setName("Corte");
        dto.setDescription("Corte de pelo");
        dto.setDurationMinutes(30);
        dto.setPrice(BigDecimal.valueOf(1500));

        Company company = Company.builder().id(COMPANY_ID).build();
        CompanyServiceEntity saved = CompanyServiceEntity.builder()
                .id(1L).name("Corte").company(company).isActive(true).build();

        when(companyService.getCompanyEntity(COMPANY_ID)).thenReturn(company);
        when(companyServiceRepository.save(any(CompanyServiceEntity.class))).thenReturn(saved);

        CompanyServiceResponse result = companyServiceService.createService(dto, COMPANY_ID, principal);

        assertEquals("Corte", result.getName());
    }


    @Test
    void editService_whenServiceNotFound_throwsException() {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", COMPANY_ID);
        UpdateCompanyServiceRequest dto = new UpdateCompanyServiceRequest();

        when(companyServiceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CompanyServiceNotFoundException.class, () -> companyServiceService.editService(dto, 1L, principal));
    }

    @Test
    void editService_whenAdminDoesNotOwnCompany_throwsAccessDenied() {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", 2L);
        UpdateCompanyServiceRequest dto = new UpdateCompanyServiceRequest();
        Company company = Company.builder().id(COMPANY_ID).build();
        CompanyServiceEntity service = CompanyServiceEntity.builder().id(1L).company(company).build();

        when(companyServiceRepository.findById(1L)).thenReturn(Optional.of(service));

        assertThrows(AccessDeniedException.class, () -> companyServiceService.editService(dto, 1L, principal));
    }

    @Test
    void editService_whenValidRequest_returnsUpdatedResponse() {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", COMPANY_ID);
        UpdateCompanyServiceRequest dto = new UpdateCompanyServiceRequest();
        dto.setName("Updated name");
        dto.setDescription("Updated description");
        dto.setDurationMinutes(85);
        dto.setPrice(BigDecimal.valueOf(1200));

        Company company = Company.builder()
                .id(COMPANY_ID)
                .build();
        CompanyServiceEntity service = CompanyServiceEntity.builder()
                    .id(1L)
                    .name("Old name")
                    .description("Old description")
                    .durationMinutes(20)
                    .price(BigDecimal.valueOf(1000))
                    .company(company)
                    .build();

        CompanyServiceEntity updated = CompanyServiceEntity.builder()
                            .id(1L)
                            .name("Updated name")
                            .description("Updated description")
                            .durationMinutes(85)
                            .price(BigDecimal.valueOf(1200))
                            .company(company)
                            .build();

        when(companyServiceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(companyServiceRepository.save(any(CompanyServiceEntity.class))).thenReturn(updated);

        CompanyServiceResponse result = companyServiceService.editService(dto, 1L, principal);

        assertEquals(updated.getName(), result.getName());
        assertEquals(updated.getDescription(), result.getDescription());
        assertEquals(updated.getDurationMinutes(), result.getDurationMinutes());
        assertEquals(updated.getPrice(), result.getPrice());
    }


    @Test
    void getServiceById_whenServiceNotFound_throwsException() {
        when(companyServiceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CompanyServiceNotFoundException.class, () -> companyServiceService.getServiceById(1L));
    }

    @Test
    void getServiceById_whenServiceIsInactive_throwsException() {
        Company company = Company.builder().id(COMPANY_ID).build();
        CompanyServiceEntity service = CompanyServiceEntity.builder().id(1L).company(company).isActive(false).build();

        when(companyServiceRepository.findById(1L)).thenReturn(Optional.of(service));

        assertThrows(IllegalStateException.class, () -> companyServiceService.getServiceById(1L));
    }

    @Test
    void getServiceById_whenServiceIsActive_returnsResponse() {
        Company company = Company.builder().id(COMPANY_ID).build();
        CompanyServiceEntity service = CompanyServiceEntity.builder().id(1L).name("Corte").company(company).isActive(true).build();

        when(companyServiceRepository.findById(1L)).thenReturn(Optional.of(service));

        CompanyServiceResponse result = companyServiceService.getServiceById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void activateService_whenServiceNotFound_throwsException() {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", COMPANY_ID);

        when(companyServiceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CompanyServiceNotFoundException.class, () -> companyServiceService.activateService(1L, principal));
    }

    @Test
    void activateService_whenValidRequest_setsActiveTrue() {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", COMPANY_ID);
        Company company = Company.builder().id(COMPANY_ID).build();
        CompanyServiceEntity service = CompanyServiceEntity.builder().id(1L).company(company).isActive(false).build();

        when(companyServiceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(companyServiceRepository.save(any(CompanyServiceEntity.class))).thenReturn(service);

        companyServiceService.activateService(1L, principal);

        assertEquals(true, service.getIsActive());
    }

    @Test
    void deactivateService_whenServiceNotFound_throwsException() {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", COMPANY_ID);

        when(companyServiceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CompanyServiceNotFoundException.class, () -> companyServiceService.deactivateService(1L, principal));
    }

    @Test
    void deactivateService_whenValidRequest_setsActiveFalse() {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", COMPANY_ID);
        Company company = Company.builder().id(COMPANY_ID).build();
        CompanyServiceEntity service = CompanyServiceEntity.builder().id(1L).company(company).isActive(true).build();

        when(companyServiceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(companyServiceRepository.save(any(CompanyServiceEntity.class))).thenReturn(service);

        companyServiceService.deactivateService(1L, principal);

        assertEquals(false, service.getIsActive());
    }


    @Test
    void checkOwnership_whenSuperAdmin_doesNotThrow() throws Exception {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "SUPER_ADMIN", 99L);
        Method method = getCheckOwnership();

        assertDoesNotThrow(() -> method.invoke(companyServiceService, principal, 1L));
    }

    @Test
    void checkOwnership_whenAdminOwnsCompany_doesNotThrow() throws Exception {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", 1L);
        Method method = getCheckOwnership();

        assertDoesNotThrow(() -> method.invoke(companyServiceService, principal, 1L));
    }

    @Test
    void checkOwnership_whenAdminDoesNotOwnCompany_throwsAccessDenied() throws Exception {
        UserPrincipal principal = new UserPrincipal("admin@test.com", "ADMIN", 2L);
        Method method = getCheckOwnership();

        InvocationTargetException ex = assertThrows(
            InvocationTargetException.class,
            () -> method.invoke(companyServiceService, principal, 1L)
        );
        assertThrows(AccessDeniedException.class, () -> { throw ex.getCause(); });
    }

    @Test
    void checkOwnership_whenIsUser_throwsAccessDenied() throws Exception{
        UserPrincipal principal = new UserPrincipal("user@test.com", "USER", 2L);
        Method method = getCheckOwnership();

        InvocationTargetException ex = assertThrows(
            InvocationTargetException.class,
            () -> method.invoke(companyServiceService, principal, 1L)
        );
        assertThrows(AccessDeniedException.class, () -> { throw ex.getCause(); });
    }

}
