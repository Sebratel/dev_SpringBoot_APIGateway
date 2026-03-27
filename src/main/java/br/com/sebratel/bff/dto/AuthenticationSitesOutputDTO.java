package br.com.sebratel.bff.dto;

import br.com.sebratel.bff.model.entity.AuthenticationSiteEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthenticationSitesOutputDTO {
    String city;
    String neighborhood;

    public static AuthenticationSitesOutputDTO fromEntity(AuthenticationSiteEntity authenticationSiteEntity) {
        return new AuthenticationSitesOutputDTO(
                authenticationSiteEntity.getCity(),
                authenticationSiteEntity.getNeighborhood()
        );
    }
}
