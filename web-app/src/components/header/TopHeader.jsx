// web-app/src/components/Header.jsx
import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faComment, faBell, faUser } from '@fortawesome/free-solid-svg-icons';
import { logOut } from "../../services/authenticationService";
import { getLoginUserId } from '../../services/localStorageService';
import '../../components/css/Header.css';


const Header = () => {
  const [activeDropdown, setActiveDropdown] = useState(null);
  const dropdownRef = useRef(null); // Tạo ref cho dropdown
  const navigate = useNavigate();
  const toggleDropdown = (dropdown) => {
    setActiveDropdown(activeDropdown === dropdown ? null : dropdown);
  };

  const handleClickOutside = (event) => {
    if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
      setActiveDropdown(null); // Ẩn dropdown khi click ra ngoài
    }
  };

  const handleLogout = () => {
    logOut();
    window.location.href = "/login";
  };
  const handleProfile = () => {
    const loginUserId = getLoginUserId();
    navigate(`/profile/${loginUserId}`);
  };

  useEffect(() => {
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  return (
    <header className="header">
      <div className="header-left" onClick={() => navigate("/")}>
        <h1>HFWF</h1>
      </div>
      <div className="header-center">
        <input type="text" placeholder="Tìm kiếm....... " />
      </div>
      <div className="header-right" ref={dropdownRef}>
        <div className="icon" onClick={() => toggleDropdown('messages')}>
          <FontAwesomeIcon icon={faComment}/>
          {activeDropdown === 'messages' && <div className="dropdown">Tin nhắn</div>}
        </div>
        <div className="icon" onClick={() => toggleDropdown('notifications')}>
          <FontAwesomeIcon icon={faBell}/>
          {activeDropdown === 'notifications' && <div className="dropdown">Thông báo</div>}
        </div>
        <div className="icon" onClick={() => toggleDropdown('profile')}>
          <FontAwesomeIcon icon={faUser}/>
          {activeDropdown === 'profile' && (
            <div className="dropdown">
            <div onClick={handleProfile}>Profile</div>
            <div>Settings</div>
            <div onClick={handleLogout}>Logout</div>
          </div>
          )
          }
        </div>
      </div>
    </header>
  );
};

export default Header;