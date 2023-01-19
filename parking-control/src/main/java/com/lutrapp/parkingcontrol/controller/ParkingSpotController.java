package com.lutrapp.parkingcontrol.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lutrapp.parkingcontrol.dto.ParkingSpotDto;
import com.lutrapp.parkingcontrol.model.ParkingSpotModel;
import com.lutrapp.parkingcontrol.service.ParkingSpotService;

@RestController
@CrossOrigin(origins="*", maxAge=3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

	final ParkingSpotService parkingSpotService;

	public ParkingSpotController(ParkingSpotService parkingSpotService) {
		this.parkingSpotService = parkingSpotService;
	}
	
	@PostMapping
	public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto){
		if(parkingSpotService.existsByLicencePlateCar(parkingSpotDto.getLicencePlateCar())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("CONFLICT: Licence Plate Car is already in use");
		}
		if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("CONFLICT: Parking Spot is already in use");
		}
		if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("CONFLICT: Parking Spot is already in use for this apartment");
		}
		
		var parkingSpotModel = new ParkingSpotModel();
		BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
		parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
		return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
	}
	
	@GetMapping
	public ResponseEntity<List<ParkingSpotModel>> getAllParkingSpots(){
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value="id") UUID id){
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		if(!parkingSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
		}			
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value="id") UUID id){
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		if(!parkingSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
		}
		parkingSpotService.delete(parkingSpotModelOptional.get());
		return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted sucessfully");
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Object> updateParkingSpot(@PathVariable(value="id") UUID id, @RequestBody @Valid ParkingSpotDto parkingSpotDto){
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		if(!parkingSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
		}
		
		var parkingSpotModel = parkingSpotModelOptional.get();
		parkingSpotModel.setParkingSpotNumber(parkingSpotDto.getParkingSpotNumber());
		parkingSpotModel.setLicencePlateCar(parkingSpotDto.getLicencePlateCar());
		parkingSpotModel.setModelCar(parkingSpotDto.getModelCar());
		parkingSpotModel.setBrandCar(parkingSpotDto.getBrandCar());
		parkingSpotModel.setColorCar(parkingSpotDto.getColorCar());
		parkingSpotModel.setResponsibleName(parkingSpotDto.getResponsibleName());
		parkingSpotModel.setApartment(parkingSpotDto.getApartment());
		parkingSpotModel.setBlock(parkingSpotDto.getBlock());

		//outra forma, mantendo os dados que não devem ser mudados. data e id, mas sem necessidade de setar tudo
//		var parkingSpotModel = new ParkingSpotModel();
//		BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
//		parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
//		parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());
		
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
	}
}
