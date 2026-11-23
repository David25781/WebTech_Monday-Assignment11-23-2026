import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import LibraryButton from "./LibraryButton";

function OfficeList(){
const [offices, setOffices] = useState([]);
const [search, setSearch] = useState("");
const navigate = useNavigate();


async function loadOffices(){
    try {
        const response = await axios.get("http://localhost:8089/api/office/all");
        setOffices(response.data);
    } catch (error) {
        alert("Error: " + error.message);
        console.log(error);
    }
}


useEffect(() => {
    loadOffices();
}, []);


async function deleteOffice(id){
    if(!window.confirm("Are you sure you want to delete this office?")){
        return;
    }
    try {
        const response = await axios.delete("http://localhost:8089/api/office/delete/" + id);
        alert("Office deleted successfully! Status: " + response.status);
        loadOffices();
    } catch (error) {
        alert("Error: " + error.message);
        console.log(error);
    }
}


const filteredOffices = offices.filter((office) =>
    office.name.toLowerCase().includes(search.toLowerCase()) ||
    String(office.officeNumber).includes(search)
);

return(
<div>

   <h1>All offices</h1>

      <LibraryButton buttonLabel="ADD NEW OFFICE" actionButton={() => navigate("/")}/>
      <br/><br/>

      <label>Search:</label><input type='text' value={search}
        placeholder="Office name or number"
        onChange={(e)=> setSearch(e.target.value)} /> <br/><br/>

      <table border="1" cellPadding="8" style={{ margin: "0 auto" }}>
        <thead>
            <tr>
                <th>Office Name</th>
                <th>Office Number</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>
            {filteredOffices.length === 0 ? (
                <tr>
                    <td colSpan="3">No offices found</td>
                </tr>
            ) : (
                filteredOffices.map((office) => (
    <tr key={office.id}>
     <td>{office.name}</td>
    <td>{office.officeNumber}</td>
     <td>                
<LibraryButton buttonLabel="Update" actionButton={() => {}}/>
 <LibraryButton buttonLabel="Delete" actionButton={() => deleteOffice(office.id)}/>
     </td>
     </tr>
     ))
     )}
     </tbody>
      </table>
    </div>

)


}

export default OfficeList;