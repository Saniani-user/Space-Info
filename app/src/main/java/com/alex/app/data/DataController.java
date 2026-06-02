package com.alex.app.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {
	FileRepository fileRepository;
	private final JdbcTemplate jdbcTemplate;
	
	public DataController(FileRepository fileRepository, JdbcTemplate jdbcTemplate) {
        this.fileRepository = fileRepository;
        this.jdbcTemplate = jdbcTemplate;
    }
	
	@GetMapping(value = "/elements", produces = "text/plain;charset=UTF-8")
	public String getCount() {
		long count = fileRepository.count();
		String sql = "SELECT id AS \"id\", name AS \"disk_name\" from my_files WHERE parent_id IS NULL";
		var disks = jdbcTemplate.queryForList(sql);
		String countName = "Количество элементов в базе данных: " + String.valueOf(count) + "\n";
		StringBuilder result = new StringBuilder(countName);
		result.append("Диски: \n");
		for(Map<String, Object> row :disks) {
			Object id = row.get("id");
			Object disk = row.get("disk_name");
			result
			.append("ID диска: ")
			.append(id)
			.append(", имя диска: ")
			.append(disk)
			.append("\n");
		}
		
		
		return result.toString();
	}
	
	 @GetMapping("/headers")
	    public List<String> getTableHeaders() {
	        return jdbcTemplate.query("SELECT * FROM my_files LIMIT 0", rs -> {
	            var metaData = rs.getMetaData();
	            List<String> columnNames = new ArrayList<>();
	            for (int i = 1; i <= metaData.getColumnCount(); i++) {
	                columnNames.add(metaData.getColumnName(i));
	            }
	            return columnNames;
	        });
	    }
	
	
}
