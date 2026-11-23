import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import LibraryButton from "./LibraryButton";

function Office(){
const [officeName, setOfficeName] = useState("");
const [officeNumber, setOfficeNumber] = useState("");
const navigate = useNavigate();


async function createAccount(){
    try {
        const response = await axios.post("http://localhost:8089/api/office/save",
        {
"name": officeName,
"officeNumber": officeNumber
        }
        );
        alert("Office saved successfully! Status: " + response.status);
        setOfficeName("");
        setOfficeNumber("");
    } catch (error) {
        alert("Error: " + error.message);
        console.log(error);
    }
}
return(
<div>
    
   <h1>Save office</h1>
      <label>Office Name:</label><input type='text' value={officeName}
        onChange={(e)=> setOfficeName(e.target.value)} /> <br/>
      <label>Office Number:</label><input type='text' value={officeNumber}
        onChange={(e)=> setOfficeNumber(e.target.value)} /> <br/>
      <LibraryButton  buttonLabel="SAVE OFFICE" actionButton={createAccount}/>
      <LibraryButton  buttonLabel="VIEW ALL OFFICES" actionButton={() => navigate("/list")}/>
    </div>

)


}

export default Office;