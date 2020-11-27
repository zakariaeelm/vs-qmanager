package com.carrefour.inno.qm.dao;

import com.carrefour.inno.qm.model.Employee;
import com.carrefour.inno.qm.model.Employees;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeDAO 
{
    private static Employees list = new Employees();
    
    static 
    {
        list.getEmployeeList().add(new Employee(1, "prenom", "nom", "prenom_nom@carrefour.com"));
        list.getEmployeeList().add(new Employee(2, "prenom", "nom", "prenom_nom@carrefour.com"));
        list.getEmployeeList().add(new Employee(3, "prenom", "nom", "prenom_nom@carrefour.com"));
        list.getEmployeeList().add(new Employee(4, "prenom", "nom", "prenom_nom@carrefour.com"));
    }
    
    public Employees getAllEmployees() 
    {
        return list;
    }
    
    public void addEmployee(Employee employee) {
        list.getEmployeeList().add(employee);
    }
}
