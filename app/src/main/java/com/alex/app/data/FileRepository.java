package com.alex.app.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alex.app.help.DirectoryFile;

@Repository
public interface FileRepository extends JpaRepository<DirectoryFile, Long>{
	DirectoryFile findByName(String name);
	
	List<DirectoryFile> findAllByNameIn(List<String> names);
	
	List<DirectoryFile> findByNameStartingWith(String prefix);
	
	List<DirectoryFile> findByRootDirectoryTrueOrderByNameAsc();
	
	@Query("SELECT c FROM DirectoryFile c WHERE c.parent.id = :id ORDER BY c.directory DESC, LOWER(c.name) ASC")
	List<DirectoryFile> findByParentIdOrderByDirectoryDescNameIgnoreCaseAsc(@Param("id") Long id);
	
	@Query("SELECt c FROM DirectoryFile c Where c.parent.id = :id Order BY c.directory ASC, LOWER(c.name) DESC")
	List<DirectoryFile> findByParentOrderedByNameDesc(@Param("id") Long id);
	
	@Query("SELECT c FROM DirectoryFile c WHERE c.parent.id = :id ORDER BY c.directory DESC, c.fileSize DESC, LOWER(c.name) ASC")
	List<DirectoryFile> findByParentOrderedBySizeDesc(@Param("id") Long id);
	
	@Query("SELECT c FROM DirectoryFile c WHERE c.parent.id = :id ORDER BY c.directory DESC, c.fileSize ASC, LOWER(c.name) ASC")
	List<DirectoryFile> findByParentOrderedBySizeAsc(@Param("id") Long id);
	
	@Query("SELECT c FROM DirectoryFile c WHERE c.parent.id = :id ORDER BY c.fileSize Desc, LOWER(c.name) ASC")
	List<DirectoryFile> findByParentOrderedBySizeIgnoreDirectories(@Param("id") Long id);
	
	List<DirectoryFile> findByParentId(Long id);
	
	@Query(value = """
		    WITH RECURSIVE tree (id, parent_id, name, is_open, is_root_directory, is_directory, is_scanned, file_size) AS (
	        -- Начальная точка
	        SELECT id, parent_id, name, is_open, is_root_directory, is_directory, is_scanned, file_size 
	        FROM my_files 
	        WHERE id = (SELECT parent_id FROM my_files WHERE id = :id)
	        
	        UNION ALL
	        
	        -- Рекурсивная часть
	        SELECT e.id, e.parent_id, e.name, e.is_open, e.is_root_directory, e.is_directory, e.is_scanned, e.file_size 
	        FROM my_files e
	        INNER JOIN tree t ON t.parent_id = e.id
	    )
	    SELECT * FROM tree
	    """, nativeQuery = true)
	List<DirectoryFile> getAllParent(@Param("id") Long id);
	
	@Modifying
	@Transactional
	@Query("""
			DELETE FROM DirectoryFile e WHERE e.name LIKE :prefix% AND e.name != :prefix
			"""
			)
	
	void deleteAllChildrens (@Param("prefix") String prefix);
	
}
