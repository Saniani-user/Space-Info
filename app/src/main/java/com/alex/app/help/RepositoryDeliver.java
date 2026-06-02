package com.alex.app.help;

import org.springframework.stereotype.Service;

import com.alex.app.data.FileRepository;

@Service
public class RepositoryDeliver {
	public final FileRepository repository;
	
	RepositoryDeliver(FileRepository repository){
		this.repository = repository;
	}
}
