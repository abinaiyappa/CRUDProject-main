package com.AdithyaUrs.CRUDdemo.REST;

import com.AdithyaUrs.CRUDdemo.Entity.Employee;
import com.AdithyaUrs.CRUDdemo.Service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")


public class EmployeeRestController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeRestController.class);

    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;

    @Autowired
    public EmployeeRestController(EmployeeService employeeService, ObjectMapper objectMapper) {
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/employees")
    public List<Employee> findAll() {
        logger.info("Fetching all employees...");
        return employeeService.findAll();
    }

    @GetMapping("/employees/{employeeId}")
    public Employee getEmployee(@PathVariable int employeeId) {
        logger.info("Fetching employee with ID: {}", employeeId);
        Employee theEmployee = employeeService.findById(employeeId);

        if (theEmployee == null) {
            logger.warn("Employee with ID {} not found", employeeId);
            throw new RuntimeException("Couldn't find the damn Employee");
        }

        return theEmployee;
    }

    @PostMapping("/employees")
    public Employee addEmployee(@RequestBody Employee theEmployee) {
        logger.info("Adding new employee: {}", theEmployee);

        theEmployee.setId(0); // force insert
        Employee dbEmployee = employeeService.save(theEmployee);

        logger.info("Employee saved with ID: {}", dbEmployee.getId());
        return dbEmployee;
    }

    @PutMapping("/employees")
    public Employee updateEmployee(@RequestBody Employee theEmployee) {
        logger.info("Updating employee: {}", theEmployee);

        Employee dbEmployee = employeeService.save(theEmployee);

        logger.info("Employee updated with ID: {}", dbEmployee.getId());
        return dbEmployee;
    }

    @PatchMapping("/employees/{employeeId}")
    public Employee patchEmployee(@PathVariable int employeeId, @RequestBody Map<String, Object> patchPayLoad) {
        logger.info("Partially updating employee with ID: {}", employeeId);

        Employee tempEmployee = employeeService.findById(employeeId);

        if (tempEmployee == null) {
            logger.warn("Employee with ID {} not found for patch", employeeId);
            throw new RuntimeException("Didn't find the guy u need");
        }

        if (patchPayLoad.containsKey("id")) {
            logger.error("Patch payload contains forbidden 'id' field");
            throw new RuntimeException("Employees id is mentioned , this is not allowed");
        }

        Employee patchEmployee = apply(patchPayLoad, tempEmployee);
        Employee dbEmployee = employeeService.save(patchEmployee);

        logger.info("Employee patched successfully with ID: {}", dbEmployee.getId());
        return dbEmployee;
    }

    private Employee apply(Map<String, Object> patchPayload, Employee tempEmployee) {
        ObjectNode employeeNode = objectMapper.convertValue(tempEmployee, ObjectNode.class);
        ObjectNode patchNode = objectMapper.convertValue(patchPayload, ObjectNode.class);
        employeeNode.setAll(patchNode);
        return objectMapper.convertValue(employeeNode, Employee.class);
    }

    @DeleteMapping("/employees/{employeeId}")
    public String deleteEmployeById(@PathVariable int employeeId) {
        logger.info("Attempting to delete employee with ID: {}", employeeId);

        Employee tempEmployee = employeeService.findById(employeeId);

        if (tempEmployee == null) {
            logger.warn("Employee ID {} does not exist", employeeId);
            throw new RuntimeException("The employee id does not exist :" + employeeId);
        }

        employeeService.deletedById(employeeId);
        logger.error("Deleted employee with ID: {}", employeeId);

        return "This id has been deleted :" + employeeId;
    }
}
