package io.datavines.coordinator.api.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.datavines.coordinator.CoordinatorConstants;
import io.datavines.coordinator.api.dto.project.ProjectCreate;
import io.datavines.coordinator.api.dto.project.ProjectUpdate;
import io.datavines.coordinator.repository.service.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@Api(value = "/project", tags = "project", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
@RequestMapping(value = CoordinatorConstants.BASE_API_PATH + "/project", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//@RefreshToken
public class ProjectController {

    @Resource
    private ProjectService projectService;

    @ApiOperation(value = "create project")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createDataSource(@Valid @RequestBody ProjectCreate projectCreate,
                                   @ApiIgnore BindingResult bindingResult) {
        return projectService.createProject(projectCreate);
    }

    @ApiOperation(value = "update project")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object updateDataSource(@Valid @RequestBody ProjectUpdate projectUpdate,
                                   @ApiIgnore BindingResult bindingResult) {
        return projectService.updateProject(projectUpdate);
    }

    @ApiOperation(value = "delete project")
    @DeleteMapping("/{id}")
    public Object deleteProject(@PathVariable Long id) {
        return projectService.deleteById(id);
    }

    @ApiOperation(value = "page ")
    @GetMapping
    public Object getProjects(
            @RequestParam("pageNumber") Integer pageNumber,
            @RequestParam("pageSize") Integer pageSize,
                             @RequestParam(value = "searchVal", required = false) String searchVal,
                             HttpServletRequest request) {
        return projectService.page(searchVal,1L, pageNumber, pageSize);
    }
}
