import "./Profile.css";
//Simple profile page for the user, with a profile picture, name, location, and stats. Also would include recent games, comments, and friends
function Profile() {
    return (
        // Main profile container
        <main className="profile">
            {/* Profile header section with avatar, name, location, and member since date */}
            <section className="profile-header">
                    <div className="profile-avatar">
                        {/* Placeholder for profile picture, currently just initials */}
                        <span>NM</span>
                    </div>
                <div className="profile-details">
                    <h1>Niall Mahon</h1>
                    <p>Furbo, Ireland</p>
                    <p>Member since September 2026</p>
                </div>
                {/* Edit profile button */}
                <button className="profile-edit" type="button">
                Edit Profile
                </button>
            </section>
            {/* Profile stats section with trophies, high score, games played, games won, and win rate */}
            <section className="profile-stats">
                <div>Trophies: 0</div>
                <div>High Score: 0</div>
                <div>Games Played: 989</div>
                <div>Games Won: 0</div>
                <div>Win Rate: 0%</div>
            </section>
            {/* Profile layout section with recent games, comments in the main page section of the page (75% of width) while rank and friends are in the sidebar */}
            <div className="profile-layout">
                <div className="profile-main-content">
                    <section className="profile-recent-games">
                        <h2>Recent Games</h2>

                            <div className="recent-games-list">
                            <div className="game-card">Game 1</div>
                            <div className="game-card">Game 2</div>
                            <div className="game-card">Game 3</div>
                </div>
            </section>
            
            <section className="profile-comments">
                <h2>Comments</h2>
                <p>CS.com: This guy is quite litterally the worst player to touch the game</p>
            </section>
            </div>
            
            <aside className="profile-sidebar">
                <section className='profile-rank'>
                    <h2>Rank</h2>
                </section>

                <section className="profile-friends">
                    <h2>Friends</h2>
                </section>
            </aside>
            </div>
        </main>

    );
}

export default Profile;

