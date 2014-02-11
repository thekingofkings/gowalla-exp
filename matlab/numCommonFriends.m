function [c, f1, f2] = numCommonFriends( ua, ub, frilists )

f1 = frilists(frilists(:,1)==ua, 2);
f2 = frilists(frilists(:,1)==ub, 2);
c = intersect(f1, f2);
end