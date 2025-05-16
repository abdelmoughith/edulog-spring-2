package pack.edulog.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pack.edulog.models.user.Nationality;
import pack.edulog.repositories.NationalityRepository;

@Service
@RequiredArgsConstructor
public class NationnalityService {
    private final NationalityRepository nationalityRepository;

    public Nationality getNatianalityById(Long id){
        return nationalityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nationality not found"));
    }
}
