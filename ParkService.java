package pet.park.service;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pet.park.controller.model.ContributorData;
import pet.park.controller.model.PetParkData;
import pet.park.dao.AmenityDao;
import pet.park.dao.PetParkDao;
import pet.park.entity.Amenity;
import pet.park.entity.Contributor;
import pet.park.entity.PetPark;

@Service
public class ParkService {
	@Autowired
	private pet.park.dao.ContributorDao contributorDao;
	@Autowired
	private AmenityDao amenityDao;
	@Autowired
	private PetParkDao petParkDao;

	@Transactional(readOnly = false)
	public ContributorData saveContributor(ContributorData contributorData) {
		Long contributorId = contributorData.getContributorId();
		Contributor contributor = findOrCreateContributor(contributorId, contributorData.getContributorEmail());

		setFieldsInContributor(contributor, contributorData);
		return new ContributorData(contributorDao.save(contributor));
	}// end ContributorData

	private void setFieldsInContributor(Contributor contributor, ContributorData contributorData) {
		contributor.setContributorEmail(contributorData.getContributorEmail());
		contributor.setContributorName(contributorData.getContributorName());

	}// end setFieldsInContributor

	private Contributor findOrCreateContributor(Long contributorId, String contributorEmail) {
		Contributor contributor;

		if (Objects.isNull(contributorId)) {
			Optional<Contributor> opContrib = contributorDao.findByContributorEmail(contributorEmail);
			if (opContrib.isPresent()) {
				throw new DuplicateKeyException("Contributor with email: " + contributorEmail + " already exists.");
			}
			contributor = new Contributor();
		} else {
			contributor = findContributorById(contributorId);
		}
		return contributor;
	} // end findOrCreateContributor

	private Contributor findContributorById(Long contributorId) {
		return contributorDao.findById(contributorId).orElseThrow(
				() -> new NoSuchElementException("Contributor with ID= " + contributorId + " was not found."));
	}// end findContributorById

	@Transactional(readOnly = true)
	public List<ContributorData> retrieveAllContributors() {
		List<Contributor> contributors = contributorDao.findAll();
		List<ContributorData> response = new LinkedList<>();

		for (Contributor contributor : contributors) {
			response.add(new ContributorData(contributor));
		}
		return response;
	}// end retrieveAllContributors

	@Transactional(readOnly = true)
	public ContributorData retrieveContributorById(Long contributorId) {

		Contributor contributor = findContributorById(contributorId);
		return new ContributorData(contributor);
	}// end retrieveContributorById

	@Transactional(readOnly = false)
	public void deleteContributorById(Long contributorId) {
		Contributor contributor = findContributorById(contributorId);
		contributorDao.delete(contributor);
	}// end deleteContributorById

	@Transactional(readOnly = false)
	public PetParkData savePetPark(Long contributorId, PetParkData petParkData) {
		Contributor contributor = findContributorById(contributorId);

		Set<Amenity> amenities = amenityDao.findAllByAmenityIn(petParkData.getAmenities());

		PetPark petPark = findOrCreatePetPark(petParkData.getPetParkId());
		setPetParkField(petPark, petParkData);

		petPark.setContributor(contributor);
		contributor.getPetParks().add(petPark);

		for (Amenity amenity : amenities) {
			amenity.getPetParks().add(petPark);
			petPark.getAmenities().add(amenity);
		}

		PetPark dbPetPark = petParkDao.save(petPark);
		return new PetParkData(dbPetPark);
	}// end savePetPark

	private void setPetParkField(PetPark petPark, PetParkData petParkData) {
		petPark.setCountry(petParkData.getCountry());
		petPark.setDirections(petParkData.getDirections());
		petPark.setGeolocation(petParkData.getGeolocation());
		petPark.setParkName(petParkData.getParkName());
		petPark.setPetParkId(petParkData.getPetParkId());
		petPark.setStateOrProvince(petParkData.getStateOrProvince());

	}// end setPetParkField

	private PetPark findOrCreatePetPark(Long petParkId) {
		PetPark petPark;

		if (Objects.isNull(petParkId)) {
			petPark = new PetPark();
		} else {
			petPark = findPetParkById(petParkId);
		}
		return petPark;
	}// end findOrCreatePetPark

	private PetPark findPetParkById(Long petParkId) {
		return petParkDao.findById(petParkId)
				.orElseThrow(() -> new NoSuchElementException("PetPark with ID = " + petParkId + " does not exist."));
	}// end findPetParkById

	@Transactional(readOnly = true)
	public PetParkData retrievePetParkById(Long contributorId, Long parkId) {
		findContributorById(contributorId);
		PetPark petPark = findPetParkById(parkId);
		
		if(petPark.getContributor().getContributorId() != contributorId) {
			throw new IllegalStateException("Pet Park with ID=" + parkId + " is not owned by contributor with ID= " + contributorId);
		}
		return new PetParkData(petPark);
	}

}// end
