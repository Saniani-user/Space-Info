package com.alex.app.help;

import java.io.File;

public class MyFile extends File{
	private static final long serialVersionUID = 367719687954273031L;

	public MyFile(String name){
		super(name);
	}
	
	public MyFile(File file){
		super(file.toString());
	}
	
	public MyFile(NodeInfo info) {
		super(info.getName());
	}
	
	@Override
	public MyFile getParentFile() {
		return new MyFile(super.getParentFile());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DirectoryFile df) {
			return super.equals(df.getThisFile());
		}
		else {
			return super.equals(o);
		}
	}
}
