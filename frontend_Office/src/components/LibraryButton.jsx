import React from 'react';

// The component receives buttonLabel and actionButton as props from the parent
function LibraryButton({ buttonLabel, actionButton }) {
    return (
        <button 
            onClick={actionButton} 
            style={{ padding: '8px 16px', cursor: 'pointer' }}
        >
            {buttonLabel}
        </button>
    );
}

export default LibraryButton;
